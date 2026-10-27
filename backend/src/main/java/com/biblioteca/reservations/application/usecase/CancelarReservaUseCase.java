package com.biblioteca.reservations.application.usecase;

import com.biblioteca.inventory.domain.repository.EjemplarRepository;

import com.biblioteca.reservations.application.command.CancelarReservaCommand;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class CancelarReservaUseCase {

    private final ReservaRepository reservas;
    private final java.time.Clock clock;
    private final DomainEventPublisher eventos;
    private final EjemplarRepository ejemplares;

    public CancelarReservaUseCase(ReservaRepository reservas, DomainEventPublisher eventos, EjemplarRepository ejemplares, java.time.Clock clock) {
        this.reservas = reservas;
        this.clock = clock;
        this.ejemplares = ejemplares;
        this.eventos = eventos;
    }

    public Reserva ejecutar(CancelarReservaCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        Reserva reserva = reservas.buscarPorIdParaActualizar(command.reservaId())
                .orElseThrow(() -> new NotFoundException("Reserva", command.reservaId().value()));

        if (reserva.estado() != EstadoReserva.SOLICITADA && reserva.estado() != EstadoReserva.APROBADA) {
            throw new BusinessRuleException("TRANSICION_INVALIDA",
                    "Solo se cancelan reservas solicitadas");
        }
        if (!reserva.prestatarioId().equals(command.prestatarioId())) {
            throw new BusinessRuleException("CANCELACION_NO_PERMITIDA",
                    "Solo el prestatario puede cancelar su reserva");
        }

        if (reserva.estado() == EstadoReserva.APROBADA) {
            var ejemplar = ejemplares.buscarPorIdParaActualizar(reserva.ejemplarId()).orElseThrow(() -> new NotFoundException("Ejemplar", reserva.ejemplarId().value()));
            ejemplar.liberarReserva();
            ejemplares.guardar(ejemplar);
        }
        reserva.cancelar(command.prestatarioId(), clock.instant());
        reservas.guardar(reserva);
        eventos.publicarTodos(reserva.extraerEventos());
        return reserva;
    }
}
