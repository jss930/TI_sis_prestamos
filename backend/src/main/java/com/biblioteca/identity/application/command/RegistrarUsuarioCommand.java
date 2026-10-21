package com.biblioteca.identity.application.command;

import com.biblioteca.identity.domain.model.PerfilPrestatario;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.NombreCompleto;

public record RegistrarUsuarioCommand(
        EmailAddress email,
        NombreCompleto nombreCompleto,
        PerfilPrestatario perfil
) {
}
