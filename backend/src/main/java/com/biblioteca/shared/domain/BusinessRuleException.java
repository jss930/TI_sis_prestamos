package com.biblioteca.shared.domain;

/** Una regla de negocio impide completar la operacion. */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
