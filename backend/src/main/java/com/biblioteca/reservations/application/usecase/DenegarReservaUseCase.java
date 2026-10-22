package com.biblioteca.reservations.application.usecase;

import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.reservations.application.command.DenegarReservaCommand;
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
public class DenegarReservaUseCase {

    private final UsuarioRepository usuarios;
    private final ReservaRepository reservas;
    private final DomainEventPublisher eventos;

    public DenegarReservaUseCase(UsuarioRepository usuarios,
                                 ReservaRepository reservas,
                                 DomainEventPublisher eventos) {
        this.usuarios = usuarios;
        this.reservas = reservas;
        this.eventos = eventos;
    }

    public Reserva ejecutar(DenegarReservaCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        Usuario operador = usuarios.buscarPorId(command.operadorId())
                .orElseThrow(() -> new NotFoundException("Operador", command.operadorId().value()));
        if (!operador.puedeGestionarReservas() && !operador.puedeGestionarCatalogo()) {
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO",
                    "El operador no esta validado o no tiene el permiso GESTIONAR_RESERVAS");
        }

        Reserva reserva = reservas.buscarPorId(command.reservaId())
                .orElseThrow(() -> new NotFoundException("Reserva", command.reservaId().value()));

        if (reserva.estado() != EstadoReserva.SOLICITADA) {
            throw new BusinessRuleException("TRANSICION_INVALIDA",
                    "Solo se deniegan reservas solicitadas");
        }

        reserva.denegar(operador.id(), command.motivo());
        reservas.guardar(reserva);
        eventos.publicarTodos(reserva.extraerEventos());
        return reserva;
    }
}
