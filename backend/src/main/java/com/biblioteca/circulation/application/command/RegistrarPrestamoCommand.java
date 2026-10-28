package com.biblioteca.circulation.application.command;

import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import java.time.Instant;

public record RegistrarPrestamoCommand(
        UsuarioId operadorId,
        UsuarioId prestatarioId,
        EjemplarId ejemplarId,
        LugarUso lugarUso,
        Instant fechaHoraEntrega,
        Instant fechaHoraVencimiento
) {
    public RegistrarPrestamoCommand(UsuarioId operadorId, UsuarioId prestatarioId,
            EjemplarId ejemplarId, LugarUso lugarUso) {
        this(operadorId, prestatarioId, ejemplarId, lugarUso, null, null);
    }
}
