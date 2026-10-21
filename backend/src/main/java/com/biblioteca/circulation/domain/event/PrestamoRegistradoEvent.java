package com.biblioteca.circulation.domain.event;

import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;

public record PrestamoRegistradoEvent(
        PrestamoId prestamoId,
        UsuarioId operadorId,
        UsuarioId prestatarioId,
        EjemplarId ejemplarId,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento,
        LugarUso lugarUso,
        Instant occurredOn
) implements DomainEvent {
}
