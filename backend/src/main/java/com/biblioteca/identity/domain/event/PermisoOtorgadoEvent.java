package com.biblioteca.identity.domain.event;

import com.biblioteca.identity.domain.model.PermisoOperativo;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record PermisoOtorgadoEvent(
        UsuarioId usuarioId,
        PermisoOperativo permiso,
        Instant occurredOn
) implements DomainEvent {
}
