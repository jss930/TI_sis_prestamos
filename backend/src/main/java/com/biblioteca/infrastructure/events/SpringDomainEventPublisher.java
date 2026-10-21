package com.biblioteca.infrastructure.events;

import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publica los eventos en el bus de Spring. La entrega es sincrona y dentro
 * del mismo hilo; el puerto permite cambiarla por async/outbox despues.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher delegate;

    public SpringDomainEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void publicar(DomainEvent evento) {
        delegate.publishEvent(evento);
    }
}
