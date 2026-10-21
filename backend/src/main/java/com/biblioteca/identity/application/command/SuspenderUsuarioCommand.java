package com.biblioteca.identity.application.command;

import com.biblioteca.identity.domain.vo.UsuarioId;

public record SuspenderUsuarioCommand(
        UsuarioId operadorId,
        UsuarioId usuarioId,
        String motivo
) {
}
