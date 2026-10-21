package com.biblioteca.catalog.domain.model;

import com.biblioteca.shared.domain.DomainException;

public record EquipoDetalle(String marca, String modelo) implements DetalleItem {

    public EquipoDetalle {
        if (marca == null || marca.isBlank()) {
            throw new DomainException("MARCA_INVALIDA", "La marca del equipo es obligatoria");
        }
        if (modelo == null || modelo.isBlank()) {
            throw new DomainException("MODELO_INVALIDO", "El modelo del equipo es obligatorio");
        }
        marca = marca.trim();
        modelo = modelo.trim();
    }

    @Override
    public TipoItem tipo() {
        return TipoItem.EQUIPO;
    }
}
