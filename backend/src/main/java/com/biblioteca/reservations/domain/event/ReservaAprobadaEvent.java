package com.biblioteca.reservations.domain.event;

import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.DomainEvent;
import java.time.Instant;

public record ReservaAprobadaEvent(ReservaId reservaId, UsuarioId operadorId, EjemplarId ejemplarId, Instant fechaLimiteRetiro, Instant occurredOn) implements DomainEvent { }
