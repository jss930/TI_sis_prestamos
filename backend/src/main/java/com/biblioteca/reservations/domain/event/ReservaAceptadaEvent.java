package com.biblioteca.reservations.domain.event;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record ReservaAceptadaEvent(
        ReservaId reservaId,
        UsuarioId operadorId,
        PrestamoId prestamoId,
        Instant occurredOn
) implements DomainEvent {
}
