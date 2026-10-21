package com.biblioteca.circulation.domain.event;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;

public record PrestamoDevueltoEvent(
        PrestamoId prestamoId,
        UsuarioId prestatarioId,
        EjemplarId ejemplarId,
        LocalDate fechaDevolucion,
        EstadoFisico estadoFisico,
        int diasRetraso,
        Instant occurredOn
) implements DomainEvent {
}
