package com.biblioteca.support;

import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.DomainEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RecordingEventPublisher implements DomainEventPublisher {

    private final List<DomainEvent> eventos = new CopyOnWriteArrayList<>();

    @Override
    public void publicar(DomainEvent evento) {
        eventos.add(evento);
    }

    public List<DomainEvent> todos() {
        return List.copyOf(eventos);
    }

    public <T extends DomainEvent> List<T> deTipo(Class<T> tipo) {
        return eventos.stream().filter(tipo::isInstance).map(tipo::cast).toList();
    }

    public boolean contiene(Class<? extends DomainEvent> tipo) {
        return eventos.stream().anyMatch(tipo::isInstance);
    }

    public void limpiar() {
        eventos.clear();
    }
}
