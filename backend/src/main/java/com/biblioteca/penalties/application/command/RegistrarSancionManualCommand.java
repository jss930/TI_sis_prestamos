package com.biblioteca.penalties.application.command;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.domain.model.MotivoSancion;
import com.biblioteca.penalties.domain.vo.MontoMulta;

public record RegistrarSancionManualCommand(
        UsuarioId operadorId,
        UsuarioId usuarioId,
        PrestamoId prestamoId,
        MotivoSancion motivo,
        int diasSuspension,
        MontoMulta multa,
        String observaciones
) {
}
