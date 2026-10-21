package com.biblioteca.shared.domain;

import java.time.Instant;

/**
 * Hecho relevante ocurrido en el dominio.
 *
 * <p>Los eventos son inmutables y se transportan como {@code record}. Se publican
 * despues de persistir el agregado; el mecanismo de entrega es un detalle de
 * infraestructura.</p>
 */
public interface DomainEvent {

    Instant occurredOn();

    default String eventName() {
        return getClass().getSimpleName();
    }
}
