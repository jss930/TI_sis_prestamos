package com.biblioteca.penalties.domain.model;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.domain.event.SancionCumplidaEvent;
import com.biblioteca.penalties.domain.event.SancionRegistradaEvent;
import com.biblioteca.penalties.domain.policy.PoliticaSancion;
import com.biblioteca.penalties.domain.vo.MontoMulta;
import com.biblioteca.penalties.domain.vo.SancionId;
import com.biblioteca.shared.domain.AggregateRoot;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Sancion con suspension y/o multa. Una sancion ACTIVA bloquea nuevos
 * prestamos; no cambia el estado de la cuenta (eso es administrativo).
 */
public final class Sancion extends AggregateRoot<SancionId> {

    private final SancionId id;
    private final UsuarioId usuarioId;
    private final PrestamoId prestamoId;
    private final MotivoSancion motivo;
    private final int diasSuspension;
    private final MontoMulta multa;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final String observaciones;

    private EstadoSancion estado;

    private Sancion(SancionId id, UsuarioId usuarioId, PrestamoId prestamoId,
                    MotivoSancion motivo, int diasSuspension, MontoMulta multa,
                    LocalDate fechaInicio, LocalDate fechaFin, EstadoSancion estado,
                    String observaciones) {
        this.id = Objects.requireNonNull(id);
        this.usuarioId = Objects.requireNonNull(usuarioId);
        this.prestamoId = prestamoId;
        this.motivo = Objects.requireNonNull(motivo);
        this.multa = Objects.requireNonNull(multa);
        this.fechaInicio = Objects.requireNonNull(fechaInicio);
        this.fechaFin = Objects.requireNonNull(fechaFin);
        this.estado = Objects.requireNonNull(estado);
        this.observaciones = normalizar(observaciones);
        if (diasSuspension < 0) {
            throw new DomainException("DIAS_SUSPENSION_INVALIDOS",
                    "Los dias de suspension no pueden ser negativos");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new DomainException("FECHAS_SANCION_INVALIDAS",
                    "La fecha de fin no puede ser anterior al inicio");
        }
        this.diasSuspension = diasSuspension;
    }

    public static Sancion porRetraso(SancionId id, UsuarioId usuarioId, PrestamoId prestamoId,
                                     int diasRetraso, LocalDate fechaDevolucion,
                                     PoliticaSancion politica) {
        if (diasRetraso <= 0) {
            throw new DomainException("RETRASO_INVALIDO",
                    "No se sanciona un retraso de cero dias");
        }
        int dias = politica.diasSuspension(diasRetraso);
        MontoMulta multa = politica.multaPorRetraso(diasRetraso);
        LocalDate fin = fechaDevolucion.plusDays(Math.max(1, dias));
        Sancion sancion = new Sancion(id, usuarioId, prestamoId, MotivoSancion.RETRASO,
                dias, multa, fechaDevolucion, fin,
                EstadoSancion.ACTIVA, null);
        sancion.registrarEvento(new SancionRegistradaEvent(
                id, usuarioId, sancion.motivo, dias, multa, sancion.fechaFin, Instant.now()));
        return sancion;
    }

    public static Sancion manual(SancionId id, UsuarioId usuarioId, PrestamoId prestamoId,
                                 MotivoSancion motivo, int diasSuspension, MontoMulta multa,
                                 LocalDate fechaInicio, String observaciones) {
        Sancion sancion = new Sancion(id, usuarioId, prestamoId, motivo, diasSuspension,
                multa, fechaInicio, fechaInicio.plusDays(Math.max(1, diasSuspension)),
                EstadoSancion.ACTIVA, observaciones);
        sancion.registrarEvento(new SancionRegistradaEvent(
                id, usuarioId, motivo, diasSuspension, multa, sancion.fechaFin, Instant.now()));
        return sancion;
    }

    public static Sancion reconstruir(SancionId id, UsuarioId usuarioId, PrestamoId prestamoId,
                                      MotivoSancion motivo, int diasSuspension, MontoMulta multa,
                                      LocalDate fechaInicio, LocalDate fechaFin,
                                      EstadoSancion estado, String observaciones) {
        return new Sancion(id, usuarioId, prestamoId, motivo, diasSuspension, multa,
                fechaInicio, fechaFin, estado, observaciones);
    }

    public void cumplir() {
        if (estado != EstadoSancion.ACTIVA) {
            throw new BusinessRuleException("SANCION_NO_ACTIVA",
                    "Solo se pueden cumplir sanciones activas");
        }
        estado = EstadoSancion.CUMPLIDA;
        registrarEvento(new SancionCumplidaEvent(id, Instant.now()));
    }

    public void anular(String motivoAnulacion) {
        if (estado != EstadoSancion.ACTIVA) {
            throw new BusinessRuleException("SANCION_NO_ACTIVA",
                    "Solo se pueden anular sanciones activas");
        }
        if (motivoAnulacion == null || motivoAnulacion.isBlank()) {
            throw new DomainException("MOTIVO_REQUERIDO",
                    "El motivo de anulacion es obligatorio");
        }
        estado = EstadoSancion.ANULADA;
    }

    public boolean estaVigente(LocalDate fecha) {
        return estado == EstadoSancion.ACTIVA
                && !fecha.isBefore(fechaInicio)
                && fecha.isBefore(fechaFin);
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    @Override
    public SancionId id() {
        return id;
    }

    public UsuarioId usuarioId() {
        return usuarioId;
    }

    public PrestamoId prestamoId() {
        return prestamoId;
    }

    public MotivoSancion motivo() {
        return motivo;
    }

    public int diasSuspension() {
        return diasSuspension;
    }

    public MontoMulta multa() {
        return multa;
    }

    public LocalDate fechaInicio() {
        return fechaInicio;
    }

    public LocalDate fechaFin() {
        return fechaFin;
    }

    public EstadoSancion estado() {
        return estado;
    }

    public String observaciones() {
        return observaciones;
    }

    @Override
    public boolean equals(Object otro) {
        return this == otro || (otro instanceof Sancion sancion && id.equals(sancion.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
