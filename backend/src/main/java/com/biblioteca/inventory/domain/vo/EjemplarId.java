package com.biblioteca.inventory.domain.vo;

import com.biblioteca.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record EjemplarId(UUID value) implements ValueObject {

    public EjemplarId {
        Objects.requireNonNull(value, "El id del ejemplar es obligatorio");
    }

    public static EjemplarId nuevo() {
        return new EjemplarId(UUID.randomUUID());
    }

    public static EjemplarId de(UUID value) {
        return new EjemplarId(value);
    }

    public static EjemplarId de(String value) {
        try {
            return new EjemplarId(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            throw new com.biblioteca.shared.domain.DomainException(
                    "ID_INVALIDO", "Id de ejemplar invalido: " + value);
        }
    }
}
