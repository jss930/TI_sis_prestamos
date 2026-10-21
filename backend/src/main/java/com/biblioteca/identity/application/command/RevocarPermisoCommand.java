package com.biblioteca.identity.application.command;

import com.biblioteca.identity.domain.model.PermisoOperativo;
import com.biblioteca.identity.domain.vo.UsuarioId;

public record RevocarPermisoCommand(
        UsuarioId operadorId,
        UsuarioId usuarioId,
        PermisoOperativo permiso
) {
}
