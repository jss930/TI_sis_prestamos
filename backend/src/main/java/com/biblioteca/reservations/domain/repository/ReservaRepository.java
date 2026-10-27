package com.biblioteca.reservations.domain.repository;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.vo.ReservaId;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.reservations.domain.policy.PoliticaHorarioReserva;

/** Puerto de salida del contexto reservations. */
public interface ReservaRepository {

    Reserva guardar(Reserva reserva);

    Optional<Reserva> buscarPorId(ReservaId id);

    default Optional<Reserva> buscarPorIdParaActualizar(ReservaId id) { return buscarPorId(id); }

    List<Reserva> listarTodos();

    List<Reserva> listarPorPrestatario(UsuarioId prestatarioId);

    List<Reserva> listarPorPrestatarioYEstado(UsuarioId prestatarioId, EstadoReserva estado);

    List<Reserva> listarPorEstado(EstadoReserva estado);

    long contarActivasPor(UsuarioId prestatarioId);

    default boolean existeSolapamiento(EjemplarId ejemplarId, Instant inicio, Instant fin,
                                       ReservaId excluir) {
        return listarTodos().stream().anyMatch(r -> r.estado() == EstadoReserva.APROBADA
                && r.ejemplarId() != null && r.ejemplarId().equals(ejemplarId)
                && r.inicioReserva() != null && r.finReserva() != null
                && (excluir == null || !r.id().equals(excluir))
                && PoliticaHorarioReserva.seSolapan(inicio, fin, r.inicioReserva(), r.finReserva()));
    }
}
