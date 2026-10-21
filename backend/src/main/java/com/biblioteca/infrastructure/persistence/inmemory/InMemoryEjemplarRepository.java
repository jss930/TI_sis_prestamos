package com.biblioteca.infrastructure.persistence.inmemory;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryEjemplarRepository implements EjemplarRepository {

    private final Map<UUID, Ejemplar> datos = new ConcurrentHashMap<>();

    @Override
    public Ejemplar guardar(Ejemplar ejemplar) {
        datos.put(ejemplar.id().value(), ejemplar);
        return ejemplar;
    }

    @Override
    public Optional<Ejemplar> buscarPorId(EjemplarId id) {
        return Optional.ofNullable(datos.get(id.value()));
    }

    @Override
    public Optional<Ejemplar> buscarPorBarcode(Barcode barcode) {
        return datos.values().stream()
                .filter(ejemplar -> ejemplar.barcode().equals(barcode))
                .findFirst();
    }

    @Override
    public List<Ejemplar> listarTodos() {
        return List.copyOf(datos.values());
    }

    @Override
    public List<Ejemplar> listarPorItem(CatalogoItemId catalogoItemId) {
        return datos.values().stream()
                .filter(ejemplar -> ejemplar.catalogoItemId().equals(catalogoItemId))
                .toList();
    }

    @Override
    public boolean existePorBarcode(Barcode barcode) {
        return datos.values().stream()
                .anyMatch(ejemplar -> ejemplar.barcode().equals(barcode));
    }
}
