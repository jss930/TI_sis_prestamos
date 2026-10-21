package com.biblioteca.catalog.domain.model;

import com.biblioteca.shared.domain.ValueObject;

/** Metadatos propios del tipo de item. */
public sealed interface DetalleItem extends ValueObject
        permits LibroDetalle, EquipoDetalle, OtroDetalle {

    TipoItem tipo();
}
