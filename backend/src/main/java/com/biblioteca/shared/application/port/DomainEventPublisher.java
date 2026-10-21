package com.biblioteca.shared.application.port;

import com.biblioteca.shared.domain.DomainEvent;

import java.util.List;

/** Puerto de salida para publicar eventos de dominio. */
public interface DomainEventPublisher {

    void publicar(DomainEvent evento);

    default void publicarTodos(List<DomainEvent> eventos) {
        eventos.forEach(this::publicar);
    }
}
