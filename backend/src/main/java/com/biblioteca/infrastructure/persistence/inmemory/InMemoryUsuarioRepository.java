package com.biblioteca.infrastructure.persistence.inmemory;

import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.UsuarioId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUsuarioRepository implements UsuarioRepository {

    private final Map<UUID, Usuario> datos = new ConcurrentHashMap<>();

    @Override
    public Usuario guardar(Usuario usuario) {
        datos.put(usuario.id().value(), usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(UsuarioId id) {
        return Optional.ofNullable(datos.get(id.value()));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(EmailAddress email) {
        return datos.values().stream()
                .filter(usuario -> usuario.email().equals(email))
                .findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        return List.copyOf(datos.values());
    }

    @Override
    public boolean existePorEmail(EmailAddress email) {
        return datos.values().stream()
                .anyMatch(usuario -> usuario.email().equals(email));
    }
}
