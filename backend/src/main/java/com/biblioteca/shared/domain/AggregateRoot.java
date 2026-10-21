package com.biblioteca.shared.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Raiz de agregado. Concentra la identidad y los eventos de dominio que produce.
 *
 * <p>Los agregados son la unica puerta de entrada a un conjunto de invariantes.
 * Nadie fuera del agregado debe modificar su estado interno.</p>
 */
public abstract class AggregateRoot<ID> {

    private final List<DomainEvent> eventos = new ArrayList<>();

    public abstract ID id();

    protected void registrarEvento(DomainEvent evento) {
        eventos.add(Objects.requireNonNull(evento, "El evento de dominio es obligatorio"));
    }

    /** Copia inmutable de los eventos pendientes, sin vaciarlos. */
    public List<DomainEvent> eventosDeDominio() {
        return List.copyOf(eventos);
    }

    /** Devuelve los eventos pendientes y deja el agregado limpio. */
    public List<DomainEvent> extraerEventos() {
        List<DomainEvent> copia = List.copyOf(eventos);
        eventos.clear();
        return copia;
    }

    public void limpiarEventos() {
        eventos.clear();
    }
}
