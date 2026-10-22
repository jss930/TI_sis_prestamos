package com.biblioteca.reservations.application.command;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.identity.domain.vo.UsuarioId;

public record SolicitarReservaCommand(UsuarioId prestatarioId, CatalogoItemId catalogoItemId) {
}
