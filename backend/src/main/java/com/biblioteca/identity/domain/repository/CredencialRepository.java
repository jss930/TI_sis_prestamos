package com.biblioteca.identity.domain.repository;

import com.biblioteca.identity.domain.vo.UsuarioId;

import java.util.Optional;

/** Puerto de salida para credenciales de acceso. */
public interface CredencialRepository {

    void guardar(UsuarioId usuarioId, String passwordHash);

    Optional<String> buscarHashPorUsuario(UsuarioId usuarioId);
}
