package com.biblioteca.infrastructure.persistence.inmemory;

import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "inmemory")
public class InMemoryPrestamoRepository implements PrestamoRepository {

    private final Map<UUID, Prestamo> datos = new ConcurrentHashMap<>();

    @Override
    public Prestamo guardar(Prestamo prestamo) {
        datos.put(prestamo.id().value(), prestamo);
        return prestamo;
    }

    @Override
    public Optional<Prestamo> buscarPorId(PrestamoId id) {
        return Optional.ofNullable(datos.get(id.value()));
    }

    @Override
    public List<Prestamo> listarTodos() {
        return List.copyOf(datos.values());
    }

    @Override
    public List<Prestamo> listarPorPrestatario(UsuarioId prestatarioId) {
        return datos.values().stream()
                .filter(prestamo -> prestamo.prestatarioId().equals(prestatarioId))
                .toList();
    }

    @Override
    public boolean existeActivoPara(EjemplarId ejemplarId) {
        return datos.values().stream()
                .anyMatch(prestamo -> prestamo.ejemplarId().equals(ejemplarId)
                        && prestamo.estado() == EstadoPrestamo.ACTIVO);
    }

    @Override
    public long contarActivosPor(UsuarioId prestatarioId) {
        return datos.values().stream()
                .filter(prestamo -> prestamo.prestatarioId().equals(prestatarioId)
                        && prestamo.estado() == EstadoPrestamo.ACTIVO)
                .count();
    }

    @Override
    public boolean tieneVencido(UsuarioId prestatarioId, LocalDate hoy) {
        return datos.values().stream()
                .anyMatch(prestamo -> prestamo.prestatarioId().equals(prestatarioId)
                        && prestamo.estado() == EstadoPrestamo.ACTIVO
                        && prestamo.periodo().estaVencido(hoy));
    }
}
