package com.biblioteca.inventory.domain.event;

import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record EjemplarRegistradoEvent(
        EjemplarId ejemplarId,
        UUID catalogoItemId,
        Barcode barcode,
        Instant occurredOn
) implements DomainEvent {
}
