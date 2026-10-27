package com.biblioteca.identity.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;
import java.util.Locale;

public record CodigoInstitucional(String value) implements ValueObject {
    public CodigoInstitucional {
        if (value == null || value.isBlank()) throw new DomainException("CODIGO_INSTITUCIONAL_REQUERIDO", "Indica el codigo universitario o de personal");
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!value.matches("[A-Z0-9-]{4,30}")) throw new DomainException("CODIGO_INSTITUCIONAL_INVALIDO", "El codigo debe tener entre 4 y 30 letras, numeros o guiones");
    }
}
