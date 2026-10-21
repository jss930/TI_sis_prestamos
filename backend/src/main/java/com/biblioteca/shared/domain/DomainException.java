package com.biblioteca.shared.domain;

/**
 * Error de negocio con un codigo estable para que la API pueda responder
 * sin acoplarse a los textos en espanol.
 */
public class DomainException extends RuntimeException {

    private final String codigo;

    public DomainException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public DomainException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
