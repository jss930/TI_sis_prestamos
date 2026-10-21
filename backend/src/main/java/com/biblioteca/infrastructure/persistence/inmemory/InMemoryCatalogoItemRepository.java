package com.biblioteca.infrastructure.persistence.inmemory;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.model.LibroDetalle;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.ISBN;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador in-memory para el corte 1. Permite ejecutar y probar el sistema
 * sin Postgres. En la fase 2 se reemplaza por un adaptador JPA.
 */
@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "inmemory")
public class InMemoryCatalogoItemRepository implements CatalogoItemRepository {

    private final Map<UUID, CatalogoItem> datos = new ConcurrentHashMap<>();

    @Override
    public CatalogoItem guardar(CatalogoItem item) {
        datos.put(item.id().value(), item);
        return item;
    }

    @Override
    public Optional<CatalogoItem> buscarPorId(CatalogoItemId id) {
        return Optional.ofNullable(datos.get(id.value()));
    }

    @Override
    public List<CatalogoItem> listarTodos() {
        return List.copyOf(datos.values());
    }

    @Override
    public boolean existePorIsbn(ISBN isbn) {
        return datos.values().stream()
                .anyMatch(item -> item.detalle() instanceof LibroDetalle libro
                        && libro.isbn().equals(isbn));
    }
}
