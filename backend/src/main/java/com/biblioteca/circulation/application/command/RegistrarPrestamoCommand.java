package com.biblioteca.circulation.application.command;

import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;

public record RegistrarPrestamoCommand(
        UsuarioId operadorId,
        UsuarioId prestatarioId,
        EjemplarId ejemplarId,
        LugarUso lugarUso
) {
}
