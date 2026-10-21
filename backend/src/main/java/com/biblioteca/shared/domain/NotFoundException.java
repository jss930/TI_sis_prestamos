package com.biblioteca.shared.domain;

/** El recurso solicitado no existe o no esta visible para el caso de uso. */
public class NotFoundException extends DomainException {

    public NotFoundException(String recurso, Object id) {
        super("RECURSO_NO_ENCONTRADO", recurso + " no encontrado: " + id);
    }
}
