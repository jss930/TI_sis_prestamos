package com.biblioteca.reservations.application.usecase;

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
    private final DomainEventPublisher eventos;

    public CancelarReservaUseCase(ReservaRepository reservas, DomainEventPublisher eventos) {
        this.reservas = reservas;
        this.eventos = eventos;
    }

    public Reserva ejecutar(CancelarReservaCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        Reserva reserva = reservas.buscarPorId(command.reservaId())
                .orElseThrow(() -> new NotFoundException("Reserva", command.reservaId().value()));

        if (reserva.estado() != EstadoReserva.SOLICITADA) {
            throw new BusinessRuleException("TRANSICION_INVALIDA",
                    "Solo se cancelan reservas solicitadas");
        }
        if (!reserva.prestatarioId().equals(command.prestatarioId())) {
            throw new BusinessRuleException("CANCELACION_NO_PERMITIDA",
                    "Solo el prestatario puede cancelar su reserva");
        }

        reserva.cancelar(command.prestatarioId());
        reservas.guardar(reserva);
        eventos.publicarTodos(reserva.extraerEventos());
        return reserva;
    }
}
