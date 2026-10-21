package com.biblioteca.identity.domain.event;

import com.biblioteca.identity.domain.model.PerfilPrestatario;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record UsuarioProvisionalRegistradoEvent(
        UsuarioId usuarioId,
        EmailAddress email,
        PerfilPrestatario perfil,
        Instant occurredOn
) implements DomainEvent {
}
