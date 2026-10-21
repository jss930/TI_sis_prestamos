package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.domain.model.EstadoCuentaUsuario;
import com.biblioteca.identity.domain.model.PerfilPrestatario;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ListarUsuariosUseCase {

    private final UsuarioRepository usuarios;

    public ListarUsuariosUseCase(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public List<Usuario> ejecutar(String texto, EstadoCuentaUsuario estado, PerfilPrestatario perfil) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return usuarios.listarTodos().stream()
                .filter(usuario -> estado == null || usuario.estado() == estado)
                .filter(usuario -> perfil == null || usuario.perfil() == perfil)
                .filter(usuario -> filtro.isEmpty()
                        || usuario.nombreCompleto().value().toLowerCase(Locale.ROOT).contains(filtro)
                        || usuario.email().value().contains(filtro))
                .toList();
    }
}
