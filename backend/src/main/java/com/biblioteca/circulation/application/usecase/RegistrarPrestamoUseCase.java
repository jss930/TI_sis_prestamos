package com.biblioteca.circulation.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.application.command.RegistrarPrestamoCommand;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.policy.PoliticaPrestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import com.biblioteca.circulation.application.port.DisponibilidadReservasPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Service
@Transactional
public class RegistrarPrestamoUseCase {

    private final UsuarioRepository usuarios;
    private final CatalogoItemRepository catalogo;
    private final EjemplarRepository ejemplares;
    private final PrestamoRepository prestamos;
    private final SancionRepository sanciones;
    private final PoliticaPrestamo politica;
    private final DomainEventPublisher eventos;
    private final Clock clock;
    private final DisponibilidadReservasPort disponibilidadReservas;

    @Autowired
    public RegistrarPrestamoUseCase(UsuarioRepository usuarios,
                                    CatalogoItemRepository catalogo,
                                    EjemplarRepository ejemplares,
                                    PrestamoRepository prestamos,
                                    SancionRepository sanciones,
                                    PoliticaPrestamo politica,
                                    DomainEventPublisher eventos,
                                    Clock clock,
                                    DisponibilidadReservasPort disponibilidadReservas) {
        this.usuarios = usuarios;
        this.catalogo = catalogo;
        this.ejemplares = ejemplares;
        this.prestamos = prestamos;
        this.sanciones = sanciones;
        this.politica = politica;
        this.eventos = eventos;
        this.clock = clock;
        this.disponibilidadReservas = disponibilidadReservas;
    }

    /** Constructor conservado para tests unitarios sin el contexto de reservas. */
    public RegistrarPrestamoUseCase(UsuarioRepository usuarios,
                                    CatalogoItemRepository catalogo,
                                    EjemplarRepository ejemplares,
                                    PrestamoRepository prestamos,
                                    SancionRepository sanciones,
                                    PoliticaPrestamo politica,
                                    DomainEventPublisher eventos,
                                    Clock clock) {
        this(usuarios, catalogo, ejemplares, prestamos, sanciones, politica, eventos, clock,
                (ejemplarId, prestatarioId, inicio, fin) -> false);
    }

    public Prestamo ejecutar(RegistrarPrestamoCommand command) {
        return ejecutar(command, null, null);
    }

    /** Registra la entrega asociada a una reserva respetando su fecha final. */
    public Prestamo ejecutarProgramado(RegistrarPrestamoCommand command,
                                       java.time.Instant inicio, java.time.Instant fin) {
        var programado = new RegistrarPrestamoCommand(command.operadorId(), command.prestatarioId(),
                command.ejemplarId(), command.lugarUso(), clock.instant(), fin);
        return ejecutar(programado, new PeriodoPrestamo(
                java.time.LocalDate.ofInstant(inicio, clock.getZone()),
                java.time.LocalDate.ofInstant(fin, clock.getZone())), fin);
    }

    private Prestamo ejecutar(RegistrarPrestamoCommand command, PeriodoPrestamo periodoProgramado,
                              java.time.Instant finProgramado) {
        Objects.requireNonNull(command.lugarUso(), "El lugar de uso es obligatorio");
        LocalDate hoy = LocalDate.now(clock);

        Usuario prestatario = usuarios.buscarPorIdParaActualizar(command.prestatarioId())
                .orElseThrow(() -> new NotFoundException("Prestatario", command.prestatarioId().value()));
        if (!prestatario.puedeSolicitarPrestamo()) {
            throw new BusinessRuleException("USUARIO_NO_HABILITADO",
                    "La cuenta " + prestatario.estado() + " no puede solicitar prestamos");
        }

        Usuario operador = usuarios.buscarPorId(command.operadorId())
                .orElseThrow(() -> new NotFoundException("Operador", command.operadorId().value()));
        if (!operador.puedeOperarVentanilla()) {
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO",
                    "El operador no esta validado o no tiene el permiso REGISTRAR_PRESTAMO");
        }
        if (operador.id().equals(prestatario.id())) {
            throw new BusinessRuleException("AUTOPRESTAMO_NO_PERMITIDO",
                    "El operador no puede registrar un prestamo a su propia cuenta");
        }

        Ejemplar ejemplar = ejemplares.buscarPorIdParaActualizar(command.ejemplarId())
                .orElseThrow(() -> new NotFoundException("Ejemplar", command.ejemplarId().value()));

        CatalogoItem item = catalogo.buscarPorId(ejemplar.catalogoItemId())
                .orElseThrow(() -> new NotFoundException(
                        "Item de catalogo", ejemplar.catalogoItemId().value()));
        if (!item.activo()) {
            throw new BusinessRuleException("ITEM_INACTIVO", "El item esta fuera de circulacion");
        }

        if (!ejemplar.estaDisponible()) {
            throw new BusinessRuleException("EJEMPLAR_NO_DISPONIBLE",
                    "El ejemplar " + ejemplar.barcode().value() + " no esta disponible");
        }
        if (prestamos.existeActivoPara(ejemplar.id())) {
            throw new BusinessRuleException("EJEMPLAR_YA_PRESTADO",
                    "El ejemplar ya tiene un prestamo activo");
        }

        if (sanciones.existeActivaPara(prestatario.id(), hoy)) {
            throw new BusinessRuleException("SANCION_ACTIVA",
                    "El prestatario tiene una sancion activa");
        }
        if (prestamos.tieneVencido(prestatario.id(), hoy)) {
            throw new BusinessRuleException("PRESTAMO_VENCIDO_PENDIENTE",
                    "El prestatario tiene un prestamo vencido sin devolver");
        }
        if (prestamos.contarActivosPor(prestatario.id())
                >= politica.maxPrestamosSimultaneos(prestatario.perfil())) {
            throw new BusinessRuleException("LIMITE_PRESTAMOS",
                    "El prestatario alcanzo el maximo de prestamos simultaneos");
        }
        if (command.lugarUso() == LugarUso.DOMICILIO
                && !politica.permiteDomicilio(prestatario.perfil(), item.tipo())) {
            throw new BusinessRuleException("DOMICILIO_NO_PERMITIDO",
                    "El perfil " + prestatario.perfil() + " no puede llevar " + item.tipo() + " a domicilio");
        }

        int dias = politica.diasPrestamo(prestatario.perfil(), item.tipo());
        Instant ahora = clock.instant();
        Instant entrega = command.fechaHoraEntrega() == null ? ahora : command.fechaHoraEntrega();
        if (entrega.isAfter(ahora.plus(Duration.ofMinutes(5)))) {
            throw new BusinessRuleException("ENTREGA_FUTURA_NO_PERMITIDA",
                    "La entrega presencial no puede registrarse en el futuro");
        }
        LocalDate fechaEntrega = LocalDate.ofInstant(entrega, clock.getZone());
        PeriodoPrestamo periodo = periodoProgramado == null
                ? PeriodoPrestamo.desde(fechaEntrega, dias)
                : periodoProgramado;
        Instant limitePolitica = periodo.vencimiento().atTime(LocalTime.of(20, 0))
                .atZone(clock.getZone()).toInstant();
        Instant finPrevisto = command.fechaHoraVencimiento() == null
                ? (finProgramado == null ? limitePolitica : finProgramado)
                : command.fechaHoraVencimiento();
        if (!finPrevisto.isAfter(entrega)) {
            throw new BusinessRuleException("VENCIMIENTO_INVALIDO",
                    "La devolución prevista debe ser posterior a la entrega");
        }
        if (periodoProgramado == null && finPrevisto.isAfter(limitePolitica)) {
            throw new BusinessRuleException("PLAZO_PRESTAMO_EXCEDIDO",
                    "La devolución prevista supera el plazo permitido para este bien");
        }
        if (disponibilidadReservas.existeConflicto(ejemplar.id(), prestatario.id(), entrega, finPrevisto)) {
            throw new BusinessRuleException("EJEMPLAR_RESERVADO_EN_HORARIO",
                    "El ejemplar tiene una reserva aprobada dentro del periodo del prestamo");
        }

        Prestamo prestamo = Prestamo.registrar(operador.id(), prestatario.id(), ejemplar.id(),
                periodo, command.lugarUso(), entrega, finPrevisto);

        ejemplar.prestar();
        prestamos.guardar(prestamo);
        ejemplares.guardar(ejemplar);

        eventos.publicarTodos(prestamo.extraerEventos());
        eventos.publicarTodos(ejemplar.extraerEventos());
        return prestamo;
    }
}
