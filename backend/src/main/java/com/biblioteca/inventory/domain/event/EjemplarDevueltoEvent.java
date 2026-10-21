package com.biblioteca.inventory.domain.event;

import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record EjemplarDevueltoEvent(
        EjemplarId ejemplarId,
        EstadoFisico estadoFisico,
        Instant occurredOn
) implements DomainEvent {
}
