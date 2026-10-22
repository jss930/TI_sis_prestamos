package com.biblioteca.reservations.domain.model;

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

import java.time.Instant;
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

    public void cancelar(UsuarioId prestatarioId) {
        exigirEstado(EstadoReserva.SOLICITADA, "CANCELAR");
        if (!this.prestatarioId.equals(prestatarioId)) {
            throw new BusinessRuleException("CANCELACION_NO_PERMITIDA",
                    "Solo el prestatario puede cancelar su reserva");
        }
        this.estado = EstadoReserva.CANCELADA;
        this.fechaDecision = Instant.now();
        registrarEvento(new ReservaCanceladaEvent(id, prestatarioId, fechaDecision));
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
            case ACEPTADA -> {
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
                if (operadorId != null || prestamoId != null || motivo != null) {
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
