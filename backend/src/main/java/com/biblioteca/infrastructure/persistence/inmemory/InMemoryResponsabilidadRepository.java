package com.biblioteca.infrastructure.persistence.inmemory;

import com.biblioteca.identity.domain.model.*;
import com.biblioteca.identity.domain.repository.ResponsabilidadRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "inmemory")
public class InMemoryResponsabilidadRepository implements ResponsabilidadRepository {
    private final Map<UsuarioId, ResponsabilidadUsuario> datos = new ConcurrentHashMap<>();
    private final Map<UUID, CambioResponsable> cambios = new ConcurrentHashMap<>();
    public Optional<ResponsabilidadUsuario> buscar(UsuarioId id) { return Optional.ofNullable(datos.get(id)); }
    public List<ResponsabilidadUsuario> listar() { return List.copyOf(datos.values()); }
    public void guardar(ResponsabilidadUsuario responsabilidad) { datos.put(responsabilidad.usuarioId(), responsabilidad); }
    public void registrarCambio(CambioResponsable cambio) { cambios.put(cambio.id(), cambio); }
    public List<CambioResponsable> historial() {
        return cambios.values().stream().sorted(Comparator.comparing(CambioResponsable::fecha).reversed()).toList();
    }
}
