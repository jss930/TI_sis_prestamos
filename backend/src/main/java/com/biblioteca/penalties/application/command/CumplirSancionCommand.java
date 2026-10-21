package com.biblioteca.penalties.application.command;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.domain.vo.SancionId;

public record CumplirSancionCommand(
        UsuarioId operadorId,
        SancionId sancionId
) {
}
