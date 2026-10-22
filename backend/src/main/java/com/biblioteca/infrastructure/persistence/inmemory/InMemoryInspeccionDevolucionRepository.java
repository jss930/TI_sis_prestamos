package com.biblioteca.infrastructure.persistence.inmemory;

import com.biblioteca.circulation.domain.model.InspeccionDevolucion;
import com.biblioteca.circulation.domain.repository.InspeccionDevolucionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "inmemory")
public class InMemoryInspeccionDevolucionRepository implements InspeccionDevolucionRepository {
    private final Map<UUID, InspeccionDevolucion> datos = new ConcurrentHashMap<>();
    public void guardar(InspeccionDevolucion inspeccion) { datos.put(inspeccion.prestamoId(), inspeccion); }
    public List<InspeccionDevolucion> listar() {
        return datos.values().stream().sorted(Comparator.comparing(InspeccionDevolucion::fecha).reversed()).toList();
    }
}
