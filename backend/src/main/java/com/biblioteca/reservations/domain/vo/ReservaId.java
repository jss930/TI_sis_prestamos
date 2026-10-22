package com.biblioteca.reservations.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record ReservaId(UUID value) implements ValueObject {

    public ReservaId {
        Objects.requireNonNull(value, "El id de la reserva es obligatorio");
    }

    public static ReservaId nuevo() {
        return new ReservaId(UUID.randomUUID());
    }

    public static ReservaId de(UUID value) {
        return new ReservaId(value);
    }

    public static ReservaId de(String value) {
        try {
            return new ReservaId(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("ID_INVALIDO", "Id de reserva invalido: " + value);
        }
    }
}
