package com.biblioteca.circulation.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Periodo de un prestamo. La fecha de vencimiento es inclusiva:
 * el retraso empieza el dia siguiente.
 */
public record PeriodoPrestamo(LocalDate inicio, LocalDate vencimiento) implements ValueObject {

    public PeriodoPrestamo {
        Objects.requireNonNull(inicio, "La fecha de inicio es obligatoria");
        Objects.requireNonNull(vencimiento, "La fecha de vencimiento es obligatoria");
        if (vencimiento.isBefore(inicio)) {
            throw new DomainException("PERIODO_INVALIDO",
                    "La fecha de vencimiento no puede ser anterior al inicio");
        }
    }

    public static PeriodoPrestamo desde(LocalDate inicio, int dias) {
        if (dias <= 0) {
            throw new DomainException("PERIODO_INVALIDO", "El plazo debe ser mayor a cero dias");
        }
        return new PeriodoPrestamo(inicio, inicio.plusDays(dias));
    }

    public PeriodoPrestamo renovar(int dias) {
        if (dias <= 0) {
            throw new DomainException("PERIODO_INVALIDO", "La renovacion debe ser mayor a cero dias");
        }
        return new PeriodoPrestamo(inicio, vencimiento.plusDays(dias));
    }

    public int diasTotales() {
        return (int) ChronoUnit.DAYS.between(inicio, vencimiento);
    }

    public boolean estaVencido(LocalDate fecha) {
        return fecha.isAfter(vencimiento);
    }

    public int diasRetraso(LocalDate fecha) {
        if (!estaVencido(fecha)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(vencimiento, fecha);
    }
}
