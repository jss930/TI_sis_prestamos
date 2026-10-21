package com.biblioteca.penalties.domain.event;

import com.biblioteca.penalties.domain.vo.SancionId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record SancionCumplidaEvent(
        SancionId sancionId,
        Instant occurredOn
) implements DomainEvent {
}
