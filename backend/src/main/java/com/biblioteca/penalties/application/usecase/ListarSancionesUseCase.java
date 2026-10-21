package com.biblioteca.penalties.application.usecase;

import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.domain.model.EstadoSancion;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarSancionesUseCase {

    private final SancionRepository sanciones;
    private final UsuarioRepository usuarios;

    public ListarSancionesUseCase(SancionRepository sanciones, UsuarioRepository usuarios) {
        this.sanciones = sanciones;
        this.usuarios = usuarios;
    }

    public List<Sancion> ejecutar(UsuarioId usuarioId, EstadoSancion estado) {
        if (usuarioId != null) {
            Usuario usuario = usuarios.buscarPorId(usuarioId)
                    .orElseThrow(() -> new com.biblioteca.shared.domain.NotFoundException(
                            "Usuario", usuarioId.value()));
            return sanciones.listarPorUsuario(usuario.id()).stream()
                    .filter(sancion -> estado == null || sancion.estado() == estado)
                    .toList();
        }
        return sanciones.listarTodas().stream()
                .filter(sancion -> estado == null || sancion.estado() == estado)
                .toList();
    }
}
