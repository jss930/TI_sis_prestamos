package com.biblioteca.catalog.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

/** Titulo editorial del item. */
public record Titulo(String value) implements ValueObject {

    private static final int MAXIMO = 150;

    public Titulo {
        if (value == null || value.isBlank()) {
            throw new DomainException("TITULO_INVALIDO", "El titulo es obligatorio");
        }
        value = value.trim();
        if (value.length() > MAXIMO) {
            throw new DomainException("TITULO_INVALIDO", "El titulo supera los " + MAXIMO + " caracteres");
        }
    }
}
