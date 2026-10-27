package com.biblioteca.reservations.application.command;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.circulation.domain.model.LugarUso;
import java.time.Instant;

public record SolicitarReservaCommand(UsuarioId prestatarioId, CatalogoItemId catalogoItemId,
        LugarUso lugarUso, Instant inicioReserva, Instant finReserva) {
    public SolicitarReservaCommand(UsuarioId prestatarioId, CatalogoItemId catalogoItemId) {
        this(prestatarioId, catalogoItemId, LugarUso.EN_CAMPUS, null, null);
    }
}
