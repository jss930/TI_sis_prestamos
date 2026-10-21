package com.biblioteca.catalog.domain.event;

import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.Titulo;
import com.biblioteca.shared.domain.DomainEvent;

import java.time.Instant;

public record ItemCatalogoRegistradoEvent(
        CatalogoItemId itemId,
        Titulo titulo,
        TipoItem tipo,
        Instant occurredOn
) implements DomainEvent {
}
