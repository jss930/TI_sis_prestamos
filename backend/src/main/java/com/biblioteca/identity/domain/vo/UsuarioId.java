package com.biblioteca.identity.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record UsuarioId(UUID value) implements ValueObject {

    public UsuarioId {
        Objects.requireNonNull(value, "El id de usuario es obligatorio");
    }

    public static UsuarioId nuevo() {
        return new UsuarioId(UUID.randomUUID());
    }

    public static UsuarioId de(UUID value) {
        return new UsuarioId(value);
    }

    public static UsuarioId de(String value) {
        try {
            return new UsuarioId(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("ID_INVALIDO", "Id de usuario invalido: " + value);
        }
    }
}
