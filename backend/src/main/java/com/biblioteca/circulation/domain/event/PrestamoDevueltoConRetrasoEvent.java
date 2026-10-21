package com.biblioteca.circulation.domain.event;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Evento que dispara el contexto penalties para registrar la sancion.
 * Se publica solo cuando el retraso es mayor a cero.
 */
public record PrestamoDevueltoConRetrasoEvent(
        PrestamoId prestamoId,
        UsuarioId prestatarioId,
        EjemplarId ejemplarId,
        LocalDate fechaDevolucion,
        int diasRetraso,
        Instant occurredOn
) implements DomainEvent {
}
