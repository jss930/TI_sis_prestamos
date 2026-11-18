package com.biblioteca.infrastructure.security;

import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Autentica primero; la identidad nunca proviene de un id enviado por el cliente. */
@Component
public class AccesoHistorial {
    private final UsuarioAutenticado autenticado;
    private final UsuarioRepository usuarios;

    public AccesoHistorial(UsuarioAutenticado autenticado, UsuarioRepository usuarios) {
        this.autenticado = autenticado;
        this.usuarios = usuarios;
    }

    public Usuario actor(String token) {
        return usuarios.buscarPorId(autenticado.exigir(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta ya no existe"));
    }

    public boolean puedeVerPrestamos(Usuario actor) {
        return actor.puedeAdministrarSistema() || actor.puedeOperarVentanilla() || actor.puedeRegistrarDevolucion();
    }

    public void exigirPropietarioOPersonal(Usuario actor, UsuarioId propietario) {
        if (!actor.id().equals(propietario) && !puedeVerPrestamos(actor)) prohibido();
    }

    public void exigirReporteUsuario(Usuario actor, UsuarioId propietario) {
        if (!actor.id().equals(propietario) && !actor.puedeAdministrarSistema()) prohibido();
    }

    public void exigirAdministrador(Usuario actor) {
        if (!actor.puedeAdministrarSistema()) prohibido();
    }

    public void prohibido() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para consultar o modificar este historial");
    }
}
