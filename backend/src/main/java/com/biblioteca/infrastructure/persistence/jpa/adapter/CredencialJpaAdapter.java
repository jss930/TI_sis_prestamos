package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.identity.domain.repository.CredencialRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.jpa.entity.CredencialJpaEntity;
import com.biblioteca.infrastructure.persistence.jpa.repository.CredencialJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "jpa", matchIfMissing = true)
public class CredencialJpaAdapter implements CredencialRepository {

    private final CredencialJpaRepository repositorio;

    public CredencialJpaAdapter(CredencialJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void guardar(UsuarioId usuarioId, String passwordHash) {
        CredencialJpaEntity entidad = new CredencialJpaEntity();
        entidad.usuarioId = usuarioId.value();
        entidad.passwordHash = passwordHash;
        repositorio.save(entidad);
    }

    @Override
    public Optional<String> buscarHashPorUsuario(UsuarioId usuarioId) {
        return repositorio.findById(usuarioId.value()).map(e -> e.passwordHash);
    }
}
