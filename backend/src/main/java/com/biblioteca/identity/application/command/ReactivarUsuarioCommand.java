package com.biblioteca.identity.application.command;

import com.biblioteca.identity.domain.vo.UsuarioId;

public record ReactivarUsuarioCommand(
        UsuarioId operadorId,
        UsuarioId usuarioId
) {
}
