package com.biblioteca.infrastructure.persistence.inmemory;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "inmemory")
public class InMemoryReservaRepository implements ReservaRepository {

    private final Map<UUID, Reserva> datos = new ConcurrentHashMap<>();

    @Override
    public Reserva guardar(Reserva reserva) {
        datos.put(reserva.id().value(), reserva);
        return reserva;
    }

    @Override
    public Optional<Reserva> buscarPorId(ReservaId id) {
        return Optional.ofNullable(datos.get(id.value()));
    }

    @Override
    public List<Reserva> listarTodos() {
        return List.copyOf(datos.values());
    }

    @Override
    public List<Reserva> listarPorPrestatario(UsuarioId prestatarioId) {
        return datos.values().stream()
                .filter(r -> r.prestatarioId().equals(prestatarioId))
                .toList();
    }

    @Override
    public List<Reserva> listarPorPrestatarioYEstado(UsuarioId prestatarioId, EstadoReserva estado) {
        return datos.values().stream()
                .filter(r -> r.prestatarioId().equals(prestatarioId) && r.estado() == estado)
                .toList();
    }

    @Override
    public List<Reserva> listarPorEstado(EstadoReserva estado) {
        return datos.values().stream()
                .filter(r -> r.estado() == estado)
                .toList();
    }

    @Override
    public long contarActivasPor(UsuarioId prestatarioId) {
        return datos.values().stream()
                .filter(r -> r.prestatarioId().equals(prestatarioId)
                        && r.estado() == EstadoReserva.SOLICITADA)
                .count();
    }
}
