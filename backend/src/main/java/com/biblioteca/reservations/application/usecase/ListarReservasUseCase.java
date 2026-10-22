package com.biblioteca.reservations.application.usecase;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;

import java.util.List;

@org.springframework.stereotype.Service
public class ListarReservasUseCase {

    private final ReservaRepository reservas;

    public ListarReservasUseCase(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    public List<Reserva> ejecutar(UsuarioId prestatarioId, EstadoReserva estado) {
        if (prestatarioId != null && estado != null) {
            return reservas.listarPorPrestatario(prestatarioId).stream()
                    .filter(r -> r.estado() == estado).toList();
        }
        if (prestatarioId != null) {
            return reservas.listarPorPrestatario(prestatarioId);
        }
        if (estado != null) {
            return reservas.listarPorEstado(estado);
        }
        return reservas.listarTodos();
    }
}
