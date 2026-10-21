package com.biblioteca.circulation.application.command;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.inventory.domain.model.EstadoFisico;

public record RegistrarDevolucionCommand(
        PrestamoId prestamoId,
        EstadoFisico estadoFisico
) {
}
