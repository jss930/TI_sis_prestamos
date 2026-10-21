package com.biblioteca.inventory.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

import java.util.Locale;

/**
 * Codigo de inventario unico de la unidad fisica. Es la identidad operativa
 * que se imprime en la etiqueta del ejemplar.
 */
public record Barcode(String value) implements ValueObject {

    private static final String PATRON = "^[A-Z0-9][A-Z0-9-]{2,49}$";

    public Barcode {
        if (value == null || value.isBlank()) {
            throw new DomainException("BARCODE_INVALIDO", "El codigo de inventario es obligatorio");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!value.matches(PATRON)) {
            throw new DomainException("BARCODE_INVALIDO",
                    "Codigo de inventario invalido (use letras, numeros y guiones): " + value);
        }
    }
}
