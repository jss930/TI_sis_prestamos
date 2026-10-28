package com.biblioteca.reservations.domain.model;

import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.reservations.domain.event.ReservaAprobadaEvent;
import com.biblioteca.reservations.domain.event.ReservaExpiradaEvent;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.event.ReservaAceptadaEvent;
import com.biblioteca.reservations.domain.event.ReservaCanceladaEvent;
import com.biblioteca.reservations.domain.event.ReservaDenegadaEvent;
import com.biblioteca.reservations.domain.event.ReservaSolicitadaEvent;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.domain.AggregateRoot;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.reservations.domain.policy.PoliticaHorarioReserva;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

public final class Reserva extends AggregateRoot<ReservaId> {

    private final ReservaId id;
    private final UsuarioId prestatarioId;
    private final CatalogoItemId catalogoItemId;
    private final Instant fechaAlta;

    private EstadoReserva estado;
    private UsuarioId operadorId;
    private PrestamoId prestamoId;
    private String motivo;
    private Instant fechaDecision;
    private EjemplarId ejemplarId;
    private Instant fechaLimiteRetiro;
    private Instant fechaEntrega;
    private UsuarioId entregadoPorId;
    private LugarUso lugarUso = LugarUso.EN_CAMPUS;
    private Instant inicioReserva;
    private Instant finReserva;

    private Reserva(ReservaId id, UsuarioId prestatarioId, CatalogoItemId catalogoItemId,
                    EstadoReserva estado, UsuarioId operadorId, PrestamoId prestamoId,
                    String motivo, Instant fechaAlta, Instant fechaDecision) {
        this.id = Objects.requireNonNull(id);
        this.prestatarioId = Objects.requireNonNull(prestatarioId);
        this.catalogoItemId = Objects.requireNonNull(catalogoItemId);
        this.estado = Objects.requireNonNull(estado);
        this.operadorId = operadorId;
        this.prestamoId = prestamoId;
        this.motivo = normalizar(motivo);
        this.fechaAlta = Objects.requireNonNull(fechaAlta);
        this.fechaDecision = fechaDecision;
        validarInvariantes();
    }

    public static Reserva solicitar(UsuarioId prestatarioId, CatalogoItemId catalogoItemId) {
        Reserva reserva = new Reserva(ReservaId.nuevo(), prestatarioId, catalogoItemId,
                EstadoReserva.SOLICITADA, null, null, null, Instant.now(), null);
        reserva.registrarEvento(new ReservaSolicitadaEvent(
                reserva.id, prestatarioId, catalogoItemId, reserva.fechaAlta));
        return reserva;
    }

    public static Reserva reconstruir(ReservaId id, UsuarioId prestatarioId, CatalogoItemId catalogoItemId,
                                      EstadoReserva estado, UsuarioId operadorId, PrestamoId prestamoId,
                                      String motivo, Instant fechaAlta, Instant fechaDecision) {
        return new Reserva(id, prestatarioId, catalogoItemId, estado, operadorId, prestamoId,
                motivo, fechaAlta, fechaDecision);
    }

    public static Reserva reconstruirSolicitud(ReservaId id, UsuarioId prestatarioId, CatalogoItemId catalogoItemId,
            EstadoReserva estado, UsuarioId operadorId, PrestamoId prestamoId, String motivo,
            Instant fechaAlta, Instant fechaDecision, EjemplarId ejemplarId,
            Instant fechaLimiteRetiro, Instant fechaEntrega, UsuarioId entregadoPorId,
            LugarUso lugarUso, Instant inicioReserva, Instant finReserva) {
        Reserva reserva = reconstruir(id, prestatarioId, catalogoItemId, estado, operadorId, prestamoId, motivo, fechaAlta, fechaDecision);
        reserva.ejemplarId = ejemplarId; reserva.fechaLimiteRetiro = fechaLimiteRetiro;
        reserva.fechaEntrega = fechaEntrega; reserva.entregadoPorId = entregadoPorId;
        reserva.lugarUso = Objects.requireNonNull(lugarUso);
        reserva.inicioReserva = inicioReserva;
        reserva.finReserva = finReserva;
        if ((estado == EstadoReserva.APROBADA || estado == EstadoReserva.ATENDIDA || estado == EstadoReserva.EXPIRADA)
                && (ejemplarId == null || fechaLimiteRetiro == null || operadorId == null))
            throw new DomainException("RESERVA_INCONSISTENTE", "La aprobacion requiere ejemplar, administrador y plazo");
        if (estado == EstadoReserva.ATENDIDA && (prestamoId == null || fechaEntrega == null || entregadoPorId == null))
            throw new DomainException("RESERVA_INCONSISTENTE", "La entrega requiere prestamo y administrador");
        return reserva;
    }

    /** Compatibilidad con registros creados antes de incorporar horarios. */
    public static Reserva reconstruirSolicitud(ReservaId id, UsuarioId prestatarioId, CatalogoItemId catalogoItemId,
            EstadoReserva estado, UsuarioId operadorId, PrestamoId prestamoId, String motivo,
            Instant fechaAlta, Instant fechaDecision, EjemplarId ejemplarId,
            Instant fechaLimiteRetiro, Instant fechaEntrega, UsuarioId entregadoPorId,
            LugarUso lugarUso) {
        return reconstruirSolicitud(id, prestatarioId, catalogoItemId, estado, operadorId, prestamoId,
                motivo, fechaAlta, fechaDecision, ejemplarId, fechaLimiteRetiro, fechaEntrega,
                entregadoPorId, lugarUso, null, null);
    }

    public static Reserva solicitar(UsuarioId prestatarioId, CatalogoItemId itemId,
            LugarUso lugarUso, Instant ahora) {
        Reserva reserva = new Reserva(ReservaId.nuevo(), prestatarioId, itemId,
                EstadoReserva.SOLICITADA, null, null, null, ahora, null);
        reserva.lugarUso = Objects.requireNonNull(lugarUso);
        reserva.registrarEvento(new ReservaSolicitadaEvent(reserva.id, prestatarioId, itemId, ahora));
        return reserva;
    }

    public static Reserva solicitarProgramada(UsuarioId prestatarioId, CatalogoItemId itemId,
            LugarUso lugarUso, Instant inicio, Instant fin, Instant ahora, ZoneId zona) {
        PoliticaHorarioReserva.validar(inicio, fin, ahora, zona);
        Reserva reserva = new Reserva(ReservaId.nuevo(), prestatarioId, itemId,
                EstadoReserva.SOLICITADA, null, null, null, ahora, null);
        reserva.lugarUso = Objects.requireNonNull(lugarUso);
        reserva.inicioReserva = inicio;
        reserva.finReserva = fin;
        reserva.registrarEvento(new ReservaSolicitadaEvent(reserva.id, prestatarioId, itemId, ahora));
        return reserva;
    }

    public void aprobar(UsuarioId operadorId, EjemplarId ejemplarId,
                        Instant ahora, Instant limite) {
        exigirEstado(EstadoReserva.SOLICITADA, "APROBAR");
        if (!limite.isAfter(ahora)) throw new DomainException("PLAZO_INVALIDO", "El plazo de retiro debe ser futuro");
        this.operadorId = Objects.requireNonNull(operadorId);
        this.ejemplarId = Objects.requireNonNull(ejemplarId);
        this.fechaDecision = ahora; this.fechaLimiteRetiro = limite; this.estado = EstadoReserva.APROBADA;
        registrarEvento(new ReservaAprobadaEvent(id, operadorId, ejemplarId, limite, ahora));
    }

    public void aprobarProgramada(UsuarioId operadorId, EjemplarId ejemplarId, Instant ahora) {
        exigirEstado(EstadoReserva.SOLICITADA, "APROBAR");
        if (inicioReserva == null || finReserva == null) {
            throw new DomainException("HORARIO_REQUERIDO", "La reserva necesita fecha y hora");
        }
        this.operadorId = Objects.requireNonNull(operadorId);
        this.ejemplarId = Objects.requireNonNull(ejemplarId);
        this.fechaDecision = ahora;
        this.fechaLimiteRetiro = inicioReserva.plus(PoliticaHorarioReserva.TOLERANCIA_RECOJO);
        this.estado = EstadoReserva.APROBADA;
        registrarEvento(new ReservaAprobadaEvent(id, operadorId, ejemplarId, fechaLimiteRetiro, ahora));
    }

    public void exigirRetiroVigente(Instant ahora) {
        exigirEstado(EstadoReserva.APROBADA, "ENTREGAR");
        if (inicioReserva != null && ahora.isBefore(inicioReserva)) {
            throw new BusinessRuleException("RECOJO_ANTICIPADO",
                    "El prestamo se entrega desde la hora inicial reservada");
        }
        if (!ahora.isBefore(fechaLimiteRetiro)) throw new BusinessRuleException("SOLICITUD_EXPIRADA", "El plazo para recoger el bien ha vencido");
    }

    public void reprogramar(Instant inicio, Instant fin, Instant ahora, ZoneId zona) {
        exigirEstado(EstadoReserva.SOLICITADA, "REPROGRAMAR");
        if (inicioReserva != null
                && !ahora.isBefore(inicioReserva.minus(PoliticaHorarioReserva.LIMITE_REPROGRAMACION))) {
            throw new BusinessRuleException("REPROGRAMACION_FUERA_DE_PLAZO",
                    "Solo puedes reprogramar hasta una hora antes del inicio");
        }
        PoliticaHorarioReserva.validar(inicio, fin, ahora, zona);
        this.inicioReserva = inicio;
        this.finReserva = fin;
    }

    public void entregar(UsuarioId entregadoPorId, PrestamoId prestamoId, Instant ahora) {
        exigirRetiroVigente(ahora);
        this.entregadoPorId = Objects.requireNonNull(entregadoPorId);
        this.prestamoId = Objects.requireNonNull(prestamoId);
        this.fechaEntrega = ahora; this.estado = EstadoReserva.ATENDIDA;
        registrarEvento(new ReservaAceptadaEvent(id, entregadoPorId, prestamoId, ahora));
    }

    public boolean expirar(Instant ahora) {
        if (estado != EstadoReserva.APROBADA || ahora.isBefore(fechaLimiteRetiro)) return false;
        estado = EstadoReserva.EXPIRADA;
        registrarEvento(new ReservaExpiradaEvent(id, ejemplarId, ahora));
        return true;
    }

    public EjemplarId ejemplarId() { return ejemplarId; }
    public Instant fechaLimiteRetiro() { return fechaLimiteRetiro; }
    public Instant fechaEntrega() { return fechaEntrega; }
    public UsuarioId entregadoPorId() { return entregadoPorId; }
    public LugarUso lugarUso() { return lugarUso; }
    public Instant inicioReserva() { return inicioReserva; }
    public Instant finReserva() { return finReserva; }

    public void aceptar(UsuarioId operadorId, PrestamoId prestamoId) {
        exigirEstado(EstadoReserva.SOLICITADA, "ACEPTAR");
        Objects.requireNonNull(operadorId, "El operador es obligatorio");
        Objects.requireNonNull(prestamoId, "El prestamo es obligatorio");
        this.operadorId = operadorId;
        this.prestamoId = prestamoId;
        this.estado = EstadoReserva.ACEPTADA;
        this.fechaDecision = Instant.now();
        registrarEvento(new ReservaAceptadaEvent(id, operadorId, prestamoId, fechaDecision));
    }

    public void denegar(UsuarioId operadorId, String motivo) {
        exigirEstado(EstadoReserva.SOLICITADA, "DENEGAR");
        Objects.requireNonNull(operadorId, "El operador es obligatorio");
        String motivoNormalizado = normalizar(motivo);
        if (motivoNormalizado == null) {
            throw new DomainException("MOTIVO_REQUERIDO", "El motivo de denegacion es obligatorio");
        }
        this.operadorId = operadorId;
        this.motivo = motivoNormalizado;
        this.estado = EstadoReserva.DENEGADA;
        this.fechaDecision = Instant.now();
        registrarEvento(new ReservaDenegadaEvent(id, operadorId, motivoNormalizado, fechaDecision));
    }

    public void cancelar(UsuarioId prestatarioId) { cancelar(prestatarioId, Instant.now()); }

    public void cancelar(UsuarioId prestatarioId, Instant ahora) {
        if (estado != EstadoReserva.SOLICITADA && estado != EstadoReserva.APROBADA)
            throw new BusinessRuleException("TRANSICION_INVALIDA", "Solo se cancelan solicitudes pendientes o aprobadas");
        if (!this.prestatarioId.equals(prestatarioId)) {
            throw new BusinessRuleException("CANCELACION_NO_PERMITIDA",
                    "Solo el prestatario puede cancelar su reserva");
        }
        if (this.estado == EstadoReserva.SOLICITADA) this.fechaDecision = ahora;
        this.estado = EstadoReserva.CANCELADA;
        registrarEvento(new ReservaCanceladaEvent(id, prestatarioId, ahora));
    }

    private void exigirEstado(EstadoReserva esperado, String accion) {
        if (estado != esperado) {
            throw new BusinessRuleException("TRANSICION_INVALIDA",
                    "No se puede " + accion + " una reserva en estado " + estado);
        }
    }

    private void validarInvariantes() {
        switch (estado) {
            case SOLICITADA -> {
                if (operadorId != null || prestamoId != null || motivo != null || fechaDecision != null) {
                    throw new DomainException("RESERVA_INCONSISTENTE",
                            "Una reserva solicitada no puede tener decision");
                }
            }
            case APROBADA, EXPIRADA -> {
                if (operadorId == null || fechaDecision == null || prestamoId != null || motivo != null)
                    throw new DomainException("RESERVA_INCONSISTENTE", "El apartado requiere aprobacion y no debe tener prestamo");
            }
            case ATENDIDA, ACEPTADA -> {
                if (operadorId == null || prestamoId == null || fechaDecision == null) {
                    throw new DomainException("RESERVA_INCONSISTENTE",
                            "Una reserva aceptada requiere operador, prestamo y fecha de decision");
                }
                if (motivo != null) {
                    throw new DomainException("RESERVA_INCONSISTENTE",
                            "Una reserva aceptada no tiene motivo de denegacion");
                }
            }
            case DENEGADA -> {
                if (operadorId == null || motivo == null || fechaDecision == null) {
                    throw new DomainException("RESERVA_INCONSISTENTE",
                            "Una reserva denegada requiere operador, motivo y fecha de decision");
                }
                if (prestamoId != null) {
                    throw new DomainException("RESERVA_INCONSISTENTE",
                            "Una reserva denegada no tiene prestamo");
                }
            }
            case CANCELADA -> {
                if (fechaDecision == null) {
                    throw new DomainException("RESERVA_INCONSISTENTE",
                            "Una reserva cancelada requiere fecha de decision");
                }
                if (prestamoId != null || motivo != null) {
                    throw new DomainException("RESERVA_INCONSISTENTE",
                            "Una reserva cancelada no tiene decision de operador");
                }
            }
        }
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    @Override
    public ReservaId id() {
        return id;
    }

    public UsuarioId prestatarioId() {
        return prestatarioId;
    }

    public CatalogoItemId catalogoItemId() {
        return catalogoItemId;
    }

    public EstadoReserva estado() {
        return estado;
    }

    public UsuarioId operadorId() {
        return operadorId;
    }

    public PrestamoId prestamoId() {
        return prestamoId;
    }

    public String motivo() {
        return motivo;
    }

    public Instant fechaAlta() {
        return fechaAlta;
    }

    public Instant fechaDecision() {
        return fechaDecision;
    }

    @Override
    public boolean equals(Object otro) {
        return this == otro || (otro instanceof Reserva reserva && id.equals(reserva.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
