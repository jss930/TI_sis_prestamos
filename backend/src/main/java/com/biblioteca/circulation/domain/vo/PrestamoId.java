package com.biblioteca.circulation.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record PrestamoId(UUID value) implements ValueObject {

    public PrestamoId {
        Objects.requireNonNull(value, "El id del prestamo es obligatorio");
    }

    public static PrestamoId nuevo() {
        return new PrestamoId(UUID.randomUUID());
    }

    public static PrestamoId de(UUID value) {
        return new PrestamoId(value);
    }

    public static PrestamoId de(String value) {
        try {
            return new PrestamoId(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("ID_INVALIDO", "Id de prestamo invalido: " + value);
        }
    }
}
