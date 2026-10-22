package com.biblioteca.reservations.application.command;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.vo.ReservaId;

public record DenegarReservaCommand(UsuarioId operadorId, ReservaId reservaId, String motivo) {
}
