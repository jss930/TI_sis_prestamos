package com.biblioteca.circulation.domain.event;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;

public record PrestamoRenovadoEvent(
        PrestamoId prestamoId,
        LocalDate nuevoVencimiento,
        int renovaciones,
        Instant occurredOn
) implements DomainEvent {
}
