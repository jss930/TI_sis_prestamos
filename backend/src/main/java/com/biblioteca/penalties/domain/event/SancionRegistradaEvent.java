package com.biblioteca.penalties.domain.event;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.domain.model.MotivoSancion;
import com.biblioteca.penalties.domain.vo.MontoMulta;
import com.biblioteca.penalties.domain.vo.SancionId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;

public record SancionRegistradaEvent(
        SancionId sancionId,
        UsuarioId usuarioId,
        MotivoSancion motivo,
        int diasSuspension,
        MontoMulta multa,
        LocalDate fechaFin,
        Instant occurredOn
) implements DomainEvent {
}
