package com.biblioteca.identity.domain.repository;

import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.UsuarioId;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del contexto identity. */
public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(UsuarioId id);

    default Optional<Usuario> buscarPorIdParaActualizar(UsuarioId id) {
        return buscarPorId(id);
    }

    Optional<Usuario> buscarPorEmail(EmailAddress email);

    List<Usuario> listarTodos();

    boolean existePorEmail(EmailAddress email);
}
