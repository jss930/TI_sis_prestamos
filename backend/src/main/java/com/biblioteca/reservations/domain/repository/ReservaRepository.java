package com.biblioteca.reservations.domain.repository;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.vo.ReservaId;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del contexto reservations. */
public interface ReservaRepository {

    Reserva guardar(Reserva reserva);

    Optional<Reserva> buscarPorId(ReservaId id);

    List<Reserva> listarTodos();

    List<Reserva> listarPorPrestatario(UsuarioId prestatarioId);

    List<Reserva> listarPorPrestatarioYEstado(UsuarioId prestatarioId, EstadoReserva estado);

    List<Reserva> listarPorEstado(EstadoReserva estado);

    long contarActivasPor(UsuarioId prestatarioId);
}
