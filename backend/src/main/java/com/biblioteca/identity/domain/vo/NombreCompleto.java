package com.biblioteca.identity.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

public record NombreCompleto(String value) implements ValueObject {

    private static final int MINIMO = 2;
    private static final int MAXIMO = 120;

    public NombreCompleto {
        if (value == null || value.isBlank()) {
            throw new DomainException("NOMBRE_INVALIDO", "El nombre completo es obligatorio");
        }
        value = value.trim().replaceAll("\\s+", " ");
        if (value.length() < MINIMO || value.length() > MAXIMO) {
            throw new DomainException("NOMBRE_INVALIDO",
                    "El nombre debe tener entre " + MINIMO + " y " + MAXIMO + " caracteres");
        }
    }
}
