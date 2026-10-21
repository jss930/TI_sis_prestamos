package com.biblioteca.penalties.application.listener;

import com.biblioteca.circulation.domain.event.PrestamoDevueltoConRetrasoEvent;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.inmemory.InMemorySancionRepository;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.penalties.domain.event.SancionRegistradaEvent;
import com.biblioteca.penalties.domain.policy.PoliticaSancionEstandar;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.support.Fixtures;
import com.biblioteca.support.RecordingEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrestamoDevueltoConRetrasoListenerTest {

    @Test
    void registraLaSancionUnaSolaVezPorPrestamo() {
        SancionRepository sanciones = new InMemorySancionRepository();
        RecordingEventPublisher eventos = new RecordingEventPublisher();
        var listener = new PrestamoDevueltoConRetrasoListener(
                sanciones, new PoliticaSancionEstandar(), eventos);

        var evento = new PrestamoDevueltoConRetrasoEvent(
                PrestamoId.nuevo(),
                new UsuarioId(UUID.randomUUID()),
                new EjemplarId(UUID.randomUUID()),
                Fixtures.HOY,
                5,
                Instant.now());

        listener.alDevolverConRetraso(evento);
        listener.alDevolverConRetraso(evento);

        assertThat(sanciones.listarTodas()).hasSize(1);
        var sancion = sanciones.listarTodas().getFirst();
        assertThat(sancion.diasSuspension()).isEqualTo(5);
        assertThat(sancion.multa().monto()).isEqualByComparingTo("5.00");
        assertThat(sancion.estaVigente(Fixtures.HOY)).isTrue();
        assertThat(eventos.contiene(SancionRegistradaEvent.class)).isTrue();
    }
}
