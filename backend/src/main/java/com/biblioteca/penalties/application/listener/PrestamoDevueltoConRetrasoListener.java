package com.biblioteca.penalties.application.listener;

import com.biblioteca.circulation.domain.event.PrestamoDevueltoConRetrasoEvent;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.policy.PoliticaSancion;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.penalties.domain.vo.SancionId;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacciona al evento de devolucion tardia y registra la sancion.
 *
 * <p>La entrega es in-process por ahora (sincrona) para que la demo sea
 * determinista. El desacople ya existe: circulation no conoce penalties.
 * En la fase de persistencia se cambia a {@code @TransactionalEventListener}
 * con outbox, sin tocar el dominio.</p>
 */
@Component
public class PrestamoDevueltoConRetrasoListener {

    private final SancionRepository sanciones;
    private final PoliticaSancion politica;
    private final DomainEventPublisher eventos;

    public PrestamoDevueltoConRetrasoListener(SancionRepository sanciones,
                                              PoliticaSancion politica,
                                              DomainEventPublisher eventos) {
        this.sanciones = sanciones;
        this.politica = politica;
        this.eventos = eventos;
    }

    @EventListener
    public void alDevolverConRetraso(PrestamoDevueltoConRetrasoEvent evento) {
        if (sanciones.existePorPrestamo(evento.prestamoId())) {
            return;
        }
        Sancion sancion = Sancion.porRetraso(SancionId.nuevo(), evento.prestatarioId(),
                evento.prestamoId(), evento.diasRetraso(), evento.fechaDevolucion(), politica);
        sanciones.guardar(sancion);
        eventos.publicarTodos(sancion.extraerEventos());
    }
}
