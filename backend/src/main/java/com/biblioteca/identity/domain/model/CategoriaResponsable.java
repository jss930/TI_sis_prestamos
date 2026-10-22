package com.biblioteca.identity.domain.model;

public enum CategoriaResponsable {
    BIBLIOTECA, DEPORTES, TECNOLOGIA, COMPUTACION;

    public boolean requiereCapacitacion() {
        return this == TECNOLOGIA || this == COMPUTACION;
    }
}
