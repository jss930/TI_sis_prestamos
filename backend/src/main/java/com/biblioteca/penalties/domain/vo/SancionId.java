package com.biblioteca.penalties.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record SancionId(UUID value) implements ValueObject {

    public SancionId {
        Objects.requireNonNull(value, "El id de sancion es obligatorio");
    }

    public static SancionId nuevo() {
        return new SancionId(UUID.randomUUID());
    }

    public static SancionId de(UUID value) {
        return new SancionId(value);
    }

    public static SancionId de(String value) {
        try {
            return new SancionId(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("ID_INVALIDO", "Id de sancion invalido: " + value);
        }
    }
}
