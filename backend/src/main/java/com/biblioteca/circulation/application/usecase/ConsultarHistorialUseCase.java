package com.biblioteca.circulation.application.usecase;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.model.*;
import com.biblioteca.circulation.domain.policy.PoliticaPlazoPrestamo;
import com.biblioteca.circulation.domain.repository.*;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.shared.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Modelo de lectura: no crea eventos ni duplica el historial en otra tabla. */
@Service
@Transactional(readOnly = true)
public class ConsultarHistorialUseCase {
    private final UsuarioRepository usuarios;
    private final EjemplarRepository ejemplares;
    private final CatalogoItemRepository catalogo;
    private final PrestamoRepository prestamos;
    private final ReservaRepository reservas;
    private final SancionRepository sanciones;
    private final InspeccionDevolucionRepository inspecciones;
    private final Clock clock;

    public ConsultarHistorialUseCase(UsuarioRepository usuarios, EjemplarRepository ejemplares,
            CatalogoItemRepository catalogo, PrestamoRepository prestamos, ReservaRepository reservas,
            SancionRepository sanciones, InspeccionDevolucionRepository inspecciones, Clock clock) {
        this.usuarios = usuarios; this.ejemplares = ejemplares; this.catalogo = catalogo;
        this.prestamos = prestamos; this.reservas = reservas; this.sanciones = sanciones;
        this.inspecciones = inspecciones; this.clock = clock;
    }

    public Historial porUsuario(UsuarioId id, LocalDate desde, LocalDate hasta, EstadoPrestamo estado) {
        var usuario = usuario(id);
        return consultar(usuario, null, desde, hasta, estado);
    }

    public Historial porEjemplar(EjemplarId id, LocalDate desde, LocalDate hasta, EstadoPrestamo estado) {
        var ejemplar = ejemplares.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Ejemplar", id.value()));
        return consultar(null, ejemplar, desde, hasta, estado);
    }

    private Historial consultar(Usuario usuario, Ejemplar ejemplar,
                                LocalDate desde, LocalDate hasta, EstadoPrestamo estado) {
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw new DomainException("RANGO_FECHAS_INVALIDO", "La fecha inicial no puede superar la final");
        var zona = PoliticaPlazoPrestamo.ZONA;
        var ahora = clock.instant();
        Predicate<Instant> enRango = instante -> {
            var fecha = instante.atZone(zona).toLocalDate();
            return (desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta));
        };
        var base = usuario == null ? prestamos.listarTodos() : prestamos.listarPorPrestatario(usuario.id());
        var seleccionados = base.stream()
                .filter(p -> ejemplar == null || p.ejemplarId().equals(ejemplar.id()))
                .filter(p -> enRango.test(p.fechaHoraEntrega()))
                .filter(p -> estado == null || p.estado() == estado)
                .sorted(Comparator.comparing(Prestamo::fechaHoraEntrega).reversed()).toList();
        var ids = seleccionados.stream().map(p -> p.id().value()).collect(Collectors.toSet());
        var inspeccionesPorPrestamo = inspecciones.listar().stream()
                .filter(i -> ids.contains(i.prestamoId()))
                .collect(Collectors.toMap(InspeccionDevolucion::prestamoId, i -> i));
        var personas = new HashMap<UsuarioId, String>();
        var movimientos = seleccionados.stream().map(p -> {
            var copia = ejemplares.buscarPorId(p.ejemplarId())
                    .orElseThrow(() -> new NotFoundException("Ejemplar", p.ejemplarId().value()));
            var inspeccion = inspeccionesPorPrestamo.get(p.id().value());
            var detalle = inspeccion == null ? null : new Inspeccion(
                    nombre(inspeccion.recibidoPorId(), personas), inspeccion.estadoFisico().name(),
                    inspeccion.accesoriosFaltantes(), inspeccion.observaciones(), inspeccion.fecha());
            return new Movimiento(p.id().value(), p.prestatarioId().value(), p.ejemplarId().value(),
                    nombre(p.prestatarioId().value(), personas), nombre(p.operadorId().value(), personas),
                    titulo(copia), copia.barcode().value(), p.lugarUso(), p.estado(),
                    p.fechaHoraEntrega(), p.fechaHoraVencimiento(), p.fechaHoraDevolucion(), p.renovaciones(),
                    p.estaVencido(ahora),
                    p.fechaHoraDevolucion() != null && p.fechaHoraDevolucion().isAfter(p.fechaHoraVencimiento()),
                    detalle);
        }).toList();
        var reservasBase = usuario == null ? reservas.listarTodos() : reservas.listarPorPrestatario(usuario.id());
        var solicitudes = reservasBase.stream()
                // Una solicitud sin copia asignada no pertenece a un ejemplar concreto.
                .filter(r -> ejemplar == null || ejemplar.id().equals(r.ejemplarId()))
                .filter(r -> enRango.test(r.inicioReserva() == null ? r.fechaAlta() : r.inicioReserva()))
                .sorted(Comparator.comparing(com.biblioteca.reservations.domain.model.Reserva::fechaAlta).reversed())
                .map(r -> new Solicitud(r.id().value(),
                        catalogo.buscarPorId(r.catalogoItemId()).orElseThrow().titulo().value(),
                        r.estado().name(), r.inicioReserva(), r.finReserva(), r.motivo())).toList();
        var sancionesBase = usuario == null ? sanciones.listarTodas() : sanciones.listarPorUsuario(usuario.id());
        // En el historial de ejemplar solo se incluyen sanciones ligadas a sus préstamos.
        var idsEjemplar = base.stream().filter(p -> ejemplar == null || ejemplar.id().equals(p.ejemplarId()))
                .map(p -> p.id()).collect(Collectors.toSet());
        var restricciones = sancionesBase.stream()
                .filter(s -> ejemplar == null || (s.prestamoId() != null && idsEjemplar.contains(s.prestamoId())))
                .filter(s -> (desde == null || !s.fechaInicio().isBefore(desde))
                        && (hasta == null || !s.fechaInicio().isAfter(hasta)))
                .sorted(Comparator.comparing(com.biblioteca.penalties.domain.model.Sancion::fechaInicio).reversed())
                .map(s -> new Restriccion(s.id().value(), s.motivo().name(), s.estado().name(),
                        s.fechaInicio(), s.fechaFin(), s.multa().monto(), s.observaciones(),
                        s.estaVigente(LocalDate.ofInstant(ahora, zona)))).toList();
        var resumen = new Resumen(movimientos.size(),
                movimientos.stream().filter(m -> m.estado() == EstadoPrestamo.ACTIVO).count(),
                movimientos.stream().filter(Movimiento::vencido).count(),
                movimientos.stream().filter(Movimiento::devolucionTardia).count(),
                restricciones.stream().filter(Restriccion::activa).count());
        var identidad = usuario == null ? null : new Persona(usuario.id().value(),
                usuario.nombreCompleto().value(), usuario.codigoInstitucional() == null ? null
                : usuario.codigoInstitucional().value(), usuario.perfil().name(), usuario.estado().name());
        var bien = ejemplar == null ? null : new Bien(ejemplar.id().value(), titulo(ejemplar),
                ejemplar.barcode().value(), ejemplar.estadoFisico().name());
        return new Historial(identidad, bien, ahora, desde, hasta, estado,
                resumen, movimientos, solicitudes, restricciones);
    }

    private Usuario usuario(UsuarioId id) {
        return usuarios.buscarPorId(id).orElseThrow(() -> new NotFoundException("Usuario", id.value()));
    }

    private String nombre(UUID id, Map<UsuarioId, String> cache) {
        return cache.computeIfAbsent(new UsuarioId(id), key -> usuario(key).nombreCompleto().value());
    }

    private String titulo(Ejemplar ejemplar) {
        return catalogo.buscarPorId(ejemplar.catalogoItemId()).orElseThrow().titulo().value();
    }

    public record Persona(UUID id, String nombre, String codigo, String perfil, String estado) { }
    public record Bien(UUID id, String titulo, String barcode, String estado) { }
    public record Resumen(long totalPrestamos, long activos, long vencidos, long devolucionesTardias, long sancionesActivas) { }
    public record Inspeccion(String recibidoPor, String estadoFisico, boolean accesoriosFaltantes,
                             String observaciones, Instant fecha) { }
    public record Movimiento(UUID id, UUID prestatarioId, UUID ejemplarId, String prestatario, String operador,
                             String titulo, String barcode, LugarUso lugarUso, EstadoPrestamo estado,
                             Instant entrega, Instant vencimiento, Instant devolucion, int renovaciones,
                             boolean vencido, boolean devolucionTardia, Inspeccion inspeccion) { }
    public record Solicitud(UUID id, String titulo, String estado, Instant inicio, Instant fin, String motivo) { }
    public record Restriccion(UUID id, String motivo, String estado, LocalDate inicio, LocalDate fin,
                              BigDecimal multa, String observaciones, boolean activa) { }
    public record Historial(Persona usuario, Bien ejemplar, Instant generadoEn, LocalDate desde, LocalDate hasta,
                             EstadoPrestamo estado, Resumen resumen, List<Movimiento> prestamos,
                             List<Solicitud> reservas, List<Restriccion> sanciones) { }
}
