package com.biblioteca.reservations.domain.event;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record ReservaCanceladaEvent(
        ReservaId reservaId,
        UsuarioId prestatarioId,
        Instant occurredOn
) implements DomainEvent {
}
