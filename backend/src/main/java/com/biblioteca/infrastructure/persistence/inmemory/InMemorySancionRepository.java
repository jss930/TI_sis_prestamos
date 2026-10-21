package com.biblioteca.infrastructure.persistence.inmemory;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.penalties.domain.vo.SancionId;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemorySancionRepository implements SancionRepository {

    private final Map<UUID, Sancion> datos = new ConcurrentHashMap<>();

    @Override
    public Sancion guardar(Sancion sancion) {
        datos.put(sancion.id().value(), sancion);
        return sancion;
    }

    @Override
    public Optional<Sancion> buscarPorId(SancionId id) {
        return Optional.ofNullable(datos.get(id.value()));
    }

    @Override
    public List<Sancion> listarTodas() {
        return List.copyOf(datos.values());
    }

    @Override
    public List<Sancion> listarPorUsuario(UsuarioId usuarioId) {
        return datos.values().stream()
                .filter(sancion -> sancion.usuarioId().equals(usuarioId))
                .toList();
    }

    @Override
    public boolean existeActivaPara(UsuarioId usuarioId, LocalDate fecha) {
        return datos.values().stream()
                .anyMatch(sancion -> sancion.usuarioId().equals(usuarioId)
                        && sancion.estaVigente(fecha));
    }

    @Override
    public boolean existePorPrestamo(PrestamoId prestamoId) {
        return datos.values().stream()
                .anyMatch(sancion -> prestamoId.equals(sancion.prestamoId()));
    }
}
