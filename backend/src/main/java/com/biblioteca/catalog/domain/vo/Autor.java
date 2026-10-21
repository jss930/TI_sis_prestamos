package com.biblioteca.catalog.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

public record Autor(String value) implements ValueObject {

    private static final int MAXIMO = 120;

    public Autor {
        if (value == null || value.isBlank()) {
            throw new DomainException("AUTOR_INVALIDO", "El autor es obligatorio");
        }
        value = value.trim();
        if (value.length() > MAXIMO) {
            throw new DomainException("AUTOR_INVALIDO", "El autor supera los " + MAXIMO + " caracteres");
        }
    }
}
