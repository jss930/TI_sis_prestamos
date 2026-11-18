package com.biblioteca.circulation.application.usecase;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.model.*;
import com.biblioteca.circulation.application.port.HistorialPrestamosPort;
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
    private final HistorialPrestamosPort lectura;

    public ConsultarHistorialUseCase(UsuarioRepository usuarios, EjemplarRepository ejemplares,
            CatalogoItemRepository catalogo, PrestamoRepository prestamos, ReservaRepository reservas,
            SancionRepository sanciones, InspeccionDevolucionRepository inspecciones, Clock clock,
            HistorialPrestamosPort lectura) {
        this.usuarios = usuarios; this.ejemplares = ejemplares; this.catalogo = catalogo;
        this.prestamos = prestamos; this.reservas = reservas; this.sanciones = sanciones;
        this.inspecciones = inspecciones; this.clock = clock;
        this.lectura = lectura;
    }

    public Historial porUsuario(UsuarioId id, LocalDate desde, LocalDate hasta, EstadoPrestamo estado) {
        var usuario = usuario(id);
        return consultar(usuario, null, desde, hasta, estado, 0, 10, false, "");
    }

    public Historial porEjemplar(EjemplarId id, LocalDate desde, LocalDate hasta, EstadoPrestamo estado) {
        var ejemplar = ejemplares.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Ejemplar", id.value()));
        return consultar(null, ejemplar, desde, hasta, estado, 0, 10, false, "");
    }

    public Historial porUsuario(UsuarioId id, LocalDate desde, LocalDate hasta, EstadoPrestamo estado,
                                int pagina, int tamano, boolean completo, String busqueda) {
        return consultar(usuario(id), null, desde, hasta, estado, pagina, tamano, completo, busqueda);
    }

    public Historial porEjemplar(EjemplarId id, LocalDate desde, LocalDate hasta, EstadoPrestamo estado,
                                 int pagina, int tamano, boolean completo, String busqueda) {
        return consultar(null, ejemplares.buscarPorId(id).orElseThrow(() -> new NotFoundException("Ejemplar", id.value())),
                desde, hasta, estado, pagina, tamano, completo, busqueda);
    }

    public Historial general(LocalDate desde, LocalDate hasta, EstadoPrestamo estado,
                              int pagina, int tamano, boolean completo, String busqueda) {
        return consultar(null, null, desde, hasta, estado, pagina, tamano, completo, busqueda);
    }

    private Historial consultar(Usuario usuario, Ejemplar ejemplar,
                                LocalDate desde, LocalDate hasta, EstadoPrestamo estado,
                                int pagina, int tamano, boolean completo, String busqueda) {
        if (pagina < 0 || tamano < 1 || tamano > 50 || pagina > 1000000)
            throw new DomainException("PAGINACION_INVALIDA", "La pagina debe ser positiva y el tamano entre 1 y 50");
        if (busqueda != null && busqueda.length() > 120)
            throw new DomainException("BUSQUEDA_INVALIDA", "La busqueda admite hasta 120 caracteres");
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw new DomainException("RANGO_FECHAS_INVALIDO", "La fecha inicial no puede superar la final");
        var zona = PoliticaPlazoPrestamo.ZONA;
        var ahora = clock.instant();
        Predicate<Instant> enRango = instante -> {
            var fecha = instante.atZone(zona).toLocalDate();
            return (desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta));
        };
        var resultado = lectura.consultar(new HistorialPrestamosPort.Filtro(
                usuario == null ? null : usuario.id(), ejemplar == null ? null : ejemplar.id(),
                desde == null ? null : desde.atStartOfDay(zona).toInstant(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(zona).toInstant(),
                estado, busqueda, pagina, tamano, completo, ahora));
        var seleccionados = resultado.registros();
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
                .sorted(Comparator.comparing(com.biblioteca.reservations.domain.model.Reserva::fechaAlta).reversed()
                        .thenComparing(r -> r.id().value()))
                .map(r -> new Solicitud(r.id().value(),
                        nombre(r.prestatarioId().value(), personas),
                        catalogo.buscarPorId(r.catalogoItemId()).orElseThrow().titulo().value(),
                        r.estado().name(), r.inicioReserva(), r.finReserva(), r.motivo())).toList();
        var sancionesBase = usuario == null ? sanciones.listarTodas() : sanciones.listarPorUsuario(usuario.id());
        // En el historial de ejemplar solo se incluyen sanciones ligadas a sus préstamos.
        var restricciones = sancionesBase.stream()
                .filter(s -> ejemplar == null || (s.prestamoId() != null && prestamos.buscarPorId(s.prestamoId())
                        .map(p -> ejemplar.id().equals(p.ejemplarId())).orElse(false)))
                .filter(s -> (desde == null || !s.fechaInicio().isBefore(desde))
                        && (hasta == null || !s.fechaInicio().isAfter(hasta)))
                .sorted(Comparator.comparing(com.biblioteca.penalties.domain.model.Sancion::fechaInicio).reversed())
                .map(s -> new Restriccion(s.id().value(), nombre(s.usuarioId().value(), personas), s.motivo().name(), s.estado().name(),
                        s.fechaInicio(), s.fechaFin(), s.multa().monto(), s.observaciones(),
                        s.estaVigente(LocalDate.ofInstant(ahora, zona)))).toList();
        var resumen = new Resumen(resultado.total(), resultado.activos(), resultado.vencidos(), resultado.devolucionesTardias(),
                restricciones.stream().filter(Restriccion::activa).count());
        var identidad = usuario == null ? null : new Persona(usuario.id().value(),
                usuario.nombreCompleto().value(), usuario.codigoInstitucional() == null ? null
                : usuario.codigoInstitucional().value(), usuario.perfil().name(), usuario.estado().name());
        var bien = ejemplar == null ? null : new Bien(ejemplar.id().value(), titulo(ejemplar),
                ejemplar.barcode().value(), ejemplar.estadoFisico().name());
        return new Historial(identidad, bien, ahora, desde, hasta, estado,
                resumen, movimientos, paginar(solicitudes, pagina, tamano, completo), paginar(restricciones, pagina, tamano, completo),
                pagina, tamano, resultado.total(), solicitudes.size(), restricciones.size(),
                !completo && (resultado.total() > (long) (pagina + 1) * tamano || solicitudes.size() > (long) (pagina + 1) * tamano
                        || restricciones.size() > (long) (pagina + 1) * tamano), busqueda);
    }

    private static <T> List<T> paginar(List<T> lista, int pagina, int tamano, boolean completo) {
        return completo ? lista : lista.stream().skip((long) pagina * tamano).limit(tamano).toList();
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
    public record Solicitud(UUID id, String prestatario, String titulo, String estado, Instant inicio, Instant fin, String motivo) { }
    public record Restriccion(UUID id, String persona, String motivo, String estado, LocalDate inicio, LocalDate fin,
                              BigDecimal multa, String observaciones, boolean activa) { }
    public record Historial(Persona usuario, Bien ejemplar, Instant generadoEn, LocalDate desde, LocalDate hasta,
                             EstadoPrestamo estado, Resumen resumen, List<Movimiento> prestamos,
                             List<Solicitud> reservas, List<Restriccion> sanciones,
                             int pagina, int tamano, long totalPrestamos, long totalReservas, long totalSanciones,
                             boolean hayMas, String busqueda) { }
}
