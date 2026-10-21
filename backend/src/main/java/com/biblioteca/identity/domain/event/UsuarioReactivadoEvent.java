package com.biblioteca.identity.domain.event;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record UsuarioReactivadoEvent(
        UsuarioId usuarioId,
        Instant occurredOn
) implements DomainEvent {
}
