package com.biblioteca.reservations.domain.event;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record ReservaSolicitadaEvent(
        ReservaId reservaId,
        UsuarioId prestatarioId,
        CatalogoItemId catalogoItemId,
        Instant occurredOn
) implements DomainEvent {
}
