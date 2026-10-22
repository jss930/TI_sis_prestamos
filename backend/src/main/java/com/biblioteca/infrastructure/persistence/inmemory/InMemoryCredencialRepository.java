package com.biblioteca.infrastructure.persistence.inmemory;

import com.biblioteca.identity.domain.repository.CredencialRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "inmemory")
public class InMemoryCredencialRepository implements CredencialRepository {

    private final Map<String, String> datos = new ConcurrentHashMap<>();

    @Override
    public void guardar(UsuarioId usuarioId, String passwordHash) {
        datos.put(usuarioId.value().toString(), passwordHash);
    }

    @Override
    public Optional<String> buscarHashPorUsuario(UsuarioId usuarioId) {
        return Optional.ofNullable(datos.get(usuarioId.value().toString()));
    }
}
