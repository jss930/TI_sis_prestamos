package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ObtenerUsuarioUseCase {

    private final UsuarioRepository usuarios;

    public ObtenerUsuarioUseCase(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario ejecutar(UsuarioId id) {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Usuario", id.value()));
    }
}
