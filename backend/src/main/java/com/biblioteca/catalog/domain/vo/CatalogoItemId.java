package com.biblioteca.catalog.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record CatalogoItemId(UUID value) implements ValueObject {

    public CatalogoItemId {
        Objects.requireNonNull(value, "El id del item es obligatorio");
    }

    public static CatalogoItemId nuevo() {
        return new CatalogoItemId(UUID.randomUUID());
    }

    public static CatalogoItemId de(UUID value) {
        return new CatalogoItemId(value);
    }

    public static CatalogoItemId de(String value) {
        try {
            return new CatalogoItemId(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("ID_INVALIDO", "Id de item invalido: " + value);
        }
    }
}
