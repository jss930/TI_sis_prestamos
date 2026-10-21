package com.biblioteca.catalog.domain.model;

import com.biblioteca.shared.domain.DomainException;

public record OtroDetalle(String descripcion) implements DetalleItem {

    public OtroDetalle {
        if (descripcion == null || descripcion.isBlank()) {
            throw new DomainException("DESCRIPCION_INVALIDA", "La descripcion es obligatoria");
        }
        descripcion = descripcion.trim();
    }

    @Override
    public TipoItem tipo() {
        return TipoItem.OTRO;
    }
}
