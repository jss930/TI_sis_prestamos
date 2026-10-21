package com.biblioteca.identity.application.command;

import com.biblioteca.identity.domain.vo.UsuarioId;

public record ValidarUsuarioCommand(
        UsuarioId operadorId,
        UsuarioId usuarioId
) {
}
