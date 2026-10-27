package com.biblioteca.reservations.application.usecase;

import com.biblioteca.shared.application.port.DomainEventPublisher;

import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;

@Service
public class ExpirarSolicitudUseCase {
    private final ReservaRepository reservas;
    private final EjemplarRepository ejemplares;
    private final Clock clock;
    private final DomainEventPublisher eventos;
    public ExpirarSolicitudUseCase(ReservaRepository reservas, EjemplarRepository ejemplares, Clock clock, DomainEventPublisher eventos) {
        this.reservas=reservas; this.ejemplares=ejemplares; this.clock=clock; this.eventos=eventos;
    }
    @Transactional
    public void ejecutar(ReservaId id) {
        var reserva=reservas.buscarPorIdParaActualizar(id).orElseThrow();
        if (!reserva.expirar(clock.instant())) return;
        if (reserva.inicioReserva() == null) {
            var ejemplar = ejemplares.buscarPorIdParaActualizar(reserva.ejemplarId()).orElseThrow();
            if (ejemplar.estadoFisico() == com.biblioteca.inventory.domain.model.EstadoFisico.RESERVADO) {
                ejemplar.liberarReserva();
                ejemplares.guardar(ejemplar);
            }
        }
        reservas.guardar(reserva);
        eventos.publicarTodos(reserva.extraerEventos());
    }
}
