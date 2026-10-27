package com.biblioteca.reservations.domain.event;

import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.DomainEvent;
import java.time.Instant;

public record ReservaExpiradaEvent(ReservaId reservaId, EjemplarId ejemplarId, Instant occurredOn) implements DomainEvent { }
