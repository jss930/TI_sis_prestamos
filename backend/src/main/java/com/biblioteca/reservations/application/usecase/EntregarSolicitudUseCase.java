package com.biblioteca.reservations.application.usecase;

import com.biblioteca.circulation.application.command.RegistrarPrestamoCommand;
import com.biblioteca.circulation.application.usecase.RegistrarPrestamoUseCase;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.domain.*;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;

@Service
@Transactional
public class EntregarSolicitudUseCase {
    private final ReservaRepository reservas;
    private final EjemplarRepository ejemplares;
    private final UsuarioRepository usuarios;
    private final RegistrarPrestamoUseCase prestamos;
    private final AccesoSolicitudes acceso;
    private final DomainEventPublisher eventos;
    private final Clock clock;
    public EntregarSolicitudUseCase(ReservaRepository reservas, EjemplarRepository ejemplares, UsuarioRepository usuarios,
            RegistrarPrestamoUseCase prestamos, AccesoSolicitudes acceso, DomainEventPublisher eventos, Clock clock) {
        this.reservas=reservas; this.ejemplares=ejemplares; this.usuarios=usuarios;
        this.prestamos=prestamos; this.acceso=acceso; this.eventos=eventos; this.clock=clock;
    }
    public Reserva ejecutar(UsuarioId operadorId, ReservaId id) {
        var reserva=reservas.buscarPorIdParaActualizar(id).orElseThrow(() -> new NotFoundException("Reserva", id.value()));
        acceso.exigirResponsable(operadorId, reserva);
        reserva.exigirRetiroVigente(clock.instant());
        usuarios.buscarPorIdParaActualizar(reserva.prestatarioId()).orElseThrow(() -> new NotFoundException("Usuario", reserva.prestatarioId().value()));
        var ejemplar=ejemplares.buscarPorIdParaActualizar(reserva.ejemplarId()).orElseThrow(() -> new NotFoundException("Ejemplar", reserva.ejemplarId().value()));
        ejemplar.liberarReserva();
        ejemplares.guardar(ejemplar);
        // La transaccion revierte la liberacion si alguna politica de entrega falla.
        var prestamo=prestamos.ejecutar(new RegistrarPrestamoCommand(operadorId, reserva.prestatarioId(), ejemplar.id(), reserva.lugarUso()));
        reserva.entregar(operadorId, prestamo.id(), clock.instant());
        reservas.guardar(reserva);
        eventos.publicarTodos(reserva.extraerEventos());
        return reserva;
    }
}
