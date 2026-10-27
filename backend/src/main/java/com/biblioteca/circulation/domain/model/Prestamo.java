package com.biblioteca.circulation.domain.model;

import com.biblioteca.circulation.domain.event.PrestamoDevueltoConRetrasoEvent;
import com.biblioteca.circulation.domain.event.PrestamoDevueltoEvent;
import com.biblioteca.circulation.domain.event.PrestamoRegistradoEvent;
import com.biblioteca.circulation.domain.event.PrestamoRenovadoEvent;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.AggregateRoot;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Agregado central del sistema: vincula operador, prestatario y ejemplar.
 *
 * <p>Invariante de auditoria: el operador nunca puede ser el mismo prestatario.
 * Esto bloquea autoprestamos incluso si la API recibe los ids invertidos.</p>
 */
public final class Prestamo extends AggregateRoot<PrestamoId> {

    private final PrestamoId id;
    private final UsuarioId operadorId;
    private final UsuarioId prestatarioId;
    private final EjemplarId ejemplarId;
    private final LugarUso lugarUso;
    private final Instant fechaAlta;

    private PeriodoPrestamo periodo;
    private EstadoPrestamo estado;
    private LocalDate fechaDevolucion;
    private int renovaciones;
    private String observaciones;

    private Prestamo(PrestamoId id, UsuarioId operadorId, UsuarioId prestatarioId,
                     EjemplarId ejemplarId, PeriodoPrestamo periodo, LugarUso lugarUso,
                     EstadoPrestamo estado, LocalDate fechaDevolucion, int renovaciones,
                     String observaciones, Instant fechaAlta) {
        this.id = Objects.requireNonNull(id);
        this.operadorId = Objects.requireNonNull(operadorId);
        this.prestatarioId = Objects.requireNonNull(prestatarioId);
        this.ejemplarId = Objects.requireNonNull(ejemplarId);
        this.periodo = Objects.requireNonNull(periodo);
        this.lugarUso = Objects.requireNonNull(lugarUso);
        this.estado = Objects.requireNonNull(estado);
        this.fechaDevolucion = fechaDevolucion;
        this.renovaciones = renovaciones;
        this.observaciones = normalizar(observaciones);
        this.fechaAlta = Objects.requireNonNull(fechaAlta);
        validarInvariantes();
    }

    public static Prestamo registrar(UsuarioId operadorId, UsuarioId prestatarioId,
                                     EjemplarId ejemplarId, PeriodoPrestamo periodo,
                                     LugarUso lugarUso) {
        Prestamo prestamo = new Prestamo(PrestamoId.nuevo(), operadorId, prestatarioId,
                ejemplarId, periodo, lugarUso, EstadoPrestamo.ACTIVO, null, 0, null, Instant.now());
        prestamo.registrarEvento(new PrestamoRegistradoEvent(
                prestamo.id, operadorId, prestatarioId, ejemplarId,
                periodo.inicio(), periodo.vencimiento(), lugarUso, prestamo.fechaAlta));
        return prestamo;
    }

    public static Prestamo reconstruir(PrestamoId id, UsuarioId operadorId, UsuarioId prestatarioId,
                                       EjemplarId ejemplarId, PeriodoPrestamo periodo, LugarUso lugarUso,
                                       EstadoPrestamo estado, LocalDate fechaDevolucion,
                                       int renovaciones, String observaciones, Instant fechaAlta) {
        return new Prestamo(id, operadorId, prestatarioId, ejemplarId, periodo, lugarUso,
                estado, fechaDevolucion, renovaciones, observaciones, fechaAlta);
    }

    public void registrarDevolucion(LocalDate fecha, EstadoFisico estadoFisico) {
        if (estado != EstadoPrestamo.ACTIVO) {
            throw new BusinessRuleException("PRESTAMO_NO_ACTIVO",
                    "El prestamo ya fue cerrado con estado " + estado);
        }
        Objects.requireNonNull(fecha, "La fecha de devolucion es obligatoria");
        Objects.requireNonNull(estadoFisico, "El estado fisico es obligatorio");
        if (fecha.isBefore(periodo.inicio())) {
            throw new DomainException("FECHA_INVALIDA",
                    "La devolucion no puede ser anterior al inicio del prestamo");
        }
        if (estadoFisico != EstadoFisico.DISPONIBLE
                && estadoFisico != EstadoFisico.DANADO
                && estadoFisico != EstadoFisico.PERDIDO
                && estadoFisico != EstadoFisico.EN_REVISION) {
            throw new DomainException("ESTADO_DEVOLUCION_INVALIDO",
                    "La devolucion solo admite DISPONIBLE, DANADO, PERDIDO o EN_REVISION");
        }

        int diasRetraso = periodo.diasRetraso(fecha);
        this.estado = estadoFisico == EstadoFisico.PERDIDO
                ? EstadoPrestamo.PERDIDO
                : EstadoPrestamo.DEVUELTO;
        this.fechaDevolucion = fecha;

        registrarEvento(new PrestamoDevueltoEvent(
                id, prestatarioId, ejemplarId, fecha, estadoFisico, diasRetraso, Instant.now()));

        if (diasRetraso > 0) {
            registrarEvento(new PrestamoDevueltoConRetrasoEvent(
                    id, prestatarioId, ejemplarId, fecha, diasRetraso, Instant.now()));
        }
    }

    public void renovar(LocalDate hoy, int dias) {
        if (estado != EstadoPrestamo.ACTIVO) {
            throw new BusinessRuleException("PRESTAMO_NO_ACTIVO",
                    "Solo se renuevan prestamos activos");
        }
        if (hoy.isAfter(periodo.vencimiento())) {
            throw new BusinessRuleException("PRESTAMO_VENCIDO",
                    "No se puede renovar un prestamo vencido");
        }
        periodo = periodo.renovar(dias);
        renovaciones++;
        registrarEvento(new PrestamoRenovadoEvent(id, periodo.vencimiento(), renovaciones, Instant.now()));
    }

    public boolean estaVencido(LocalDate hoy) {
        return estado == EstadoPrestamo.ACTIVO && periodo.estaVencido(hoy);
    }

    public int diasRetraso(LocalDate hoy) {
        if (fechaDevolucion != null) {
            return periodo.diasRetraso(fechaDevolucion);
        }
        return estado == EstadoPrestamo.ACTIVO ? periodo.diasRetraso(hoy) : 0;
    }

    public void registrarObservacion(String observacion) {
        this.observaciones = normalizar(observacion);
    }

    private void validarInvariantes() {
        if (operadorId.equals(prestatarioId)) {
            throw new BusinessRuleException("AUTOPRESTAMO_NO_PERMITIDO",
                    "El operador no puede ser el mismo que el prestatario");
        }
        if (renovaciones < 0) {
            throw new DomainException("RENOVACIONES_INVALIDAS",
                    "El numero de renovaciones no puede ser negativo");
        }
        if (estado == EstadoPrestamo.ACTIVO && fechaDevolucion != null) {
            throw new DomainException("PRESTAMO_INCONSISTENTE",
                    "Un prestamo activo no puede tener fecha de devolucion");
        }
        if (estado != EstadoPrestamo.ACTIVO && fechaDevolucion == null) {
            throw new DomainException("PRESTAMO_INCONSISTENTE",
                    "Un prestamo cerrado requiere fecha de devolucion");
        }
        if (fechaDevolucion != null && fechaDevolucion.isBefore(periodo.inicio())) {
            throw new DomainException("PRESTAMO_INCONSISTENTE",
                    "La fecha de devolucion no puede ser anterior al inicio");
        }
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    @Override
    public PrestamoId id() {
        return id;
    }

    public UsuarioId operadorId() {
        return operadorId;
    }

    public UsuarioId prestatarioId() {
        return prestatarioId;
    }

    public EjemplarId ejemplarId() {
        return ejemplarId;
    }

    public PeriodoPrestamo periodo() {
        return periodo;
    }

    public LugarUso lugarUso() {
        return lugarUso;
    }

    public EstadoPrestamo estado() {
        return estado;
    }

    public LocalDate fechaDevolucion() {
        return fechaDevolucion;
    }

    public int renovaciones() {
        return renovaciones;
    }

    public String observaciones() {
        return observaciones;
    }

    public Instant fechaAlta() {
        return fechaAlta;
    }

    @Override
    public boolean equals(Object otro) {
        return this == otro || (otro instanceof Prestamo prestamo && id.equals(prestamo.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
