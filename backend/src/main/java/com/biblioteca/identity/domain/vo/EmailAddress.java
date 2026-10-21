package com.biblioteca.identity.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

import java.util.Locale;
import java.util.regex.Pattern;

/** Correo institucional normalizado a minusculas. */
public record EmailAddress(String value) implements ValueObject {

    private static final Pattern PATRON =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new DomainException("EMAIL_INVALIDO", "El correo es obligatorio");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!PATRON.matcher(value).matches()) {
            throw new DomainException("EMAIL_INVALIDO", "Correo invalido: " + value);
        }
    }
}
