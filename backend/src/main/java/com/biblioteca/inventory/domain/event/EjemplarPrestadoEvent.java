package com.biblioteca.inventory.domain.event;

import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record EjemplarPrestadoEvent(
        EjemplarId ejemplarId,
        Instant occurredOn
) implements DomainEvent {
}
