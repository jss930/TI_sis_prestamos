package com.biblioteca.penalties.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

/** Monto de multa con moneda ISO de tres letras. */
public record MontoMulta(BigDecimal monto, String moneda) implements ValueObject {

    public MontoMulta {
        Objects.requireNonNull(monto, "El monto es obligatorio");
        if (monto.signum() < 0) {
            throw new DomainException("MONTO_INVALIDO", "El monto no puede ser negativo");
        }
        monto = monto.setScale(2, RoundingMode.HALF_UP);
        if (moneda == null || !moneda.matches("[A-Za-z]{3}")) {
            throw new DomainException("MONEDA_INVALIDA", "La moneda debe tener tres letras");
        }
        moneda = moneda.toUpperCase(Locale.ROOT);
    }

    public static MontoMulta cero() {
        return new MontoMulta(BigDecimal.ZERO, "PEN");
    }

    public static MontoMulta deSoles(BigDecimal monto) {
        return new MontoMulta(monto, "PEN");
    }

    public MontoMulta sumar(MontoMulta otro) {
        if (!moneda.equals(otro.moneda)) {
            throw new DomainException("MONEDAS_INCOMPATIBLES",
                    "No se pueden sumar montos en monedas distintas");
        }
        return new MontoMulta(monto.add(otro.monto), moneda);
    }
}
