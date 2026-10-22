package com.biblioteca.reservations.application.command;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.vo.ReservaId;

public record AceptarReservaCommand(UsuarioId operadorId, ReservaId reservaId,
        com.biblioteca.inventory.domain.vo.EjemplarId ejemplarId) {
    public AceptarReservaCommand(UsuarioId operadorId, ReservaId reservaId) {
        this(operadorId, reservaId, null);
    }
}
