package com.biblioteca.catalog.domain.model;

import com.biblioteca.catalog.domain.vo.Autor;
import com.biblioteca.catalog.domain.vo.ISBN;
import com.biblioteca.shared.domain.DomainException;

import java.util.Objects;

public record LibroDetalle(ISBN isbn, Autor autor, String editorial, int anioPublicacion)
        implements DetalleItem {

    public LibroDetalle {
        Objects.requireNonNull(isbn, "El ISBN es obligatorio");
        Objects.requireNonNull(autor, "El autor es obligatorio");
        if (editorial == null || editorial.isBlank()) {
            throw new DomainException("EDITORIAL_INVALIDA", "La editorial es obligatoria");
        }
        editorial = editorial.trim();
        if (anioPublicacion < 0) {
            throw new DomainException("ANIO_INVALIDO", "El anio de publicacion no puede ser negativo");
        }
    }

    @Override
    public TipoItem tipo() {
        return TipoItem.LIBRO;
    }
}
