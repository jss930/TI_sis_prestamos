package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.domain.model.*;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.*;
import com.biblioteca.shared.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;

@Service
@Transactional
public class RevisarCuentaUseCase {
    private final UsuarioRepository usuarios;
    private final Clock clock;
    public RevisarCuentaUseCase(UsuarioRepository usuarios, Clock clock) { this.usuarios=usuarios; this.clock=clock; }

    public Usuario solicitarCorreccion(UsuarioId operadorId, UsuarioId usuarioId, String observaciones) {
        var operador=obtener(operadorId);
        if (!operador.puedeAdministrarSistema() && !operador.puedeValidarProvisionales())
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO", "No tienes permiso para revisar cuentas");
        var usuario=bloquear(usuarioId);
        usuario.solicitarCorreccion(operadorId, observaciones, clock.instant());
        return usuarios.guardar(usuario);
    }

    public Usuario actualizar(UsuarioId operadorId, UsuarioId usuarioId, EmailAddress email, NombreCompleto nombre,
                              PerfilPrestatario perfil, CodigoInstitucional codigo) {
        var operador=obtener(operadorId);
        if (!operadorId.equals(usuarioId) && !operador.puedeAdministrarSistema() && !operador.puedeValidarProvisionales())
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO", "Solo puedes corregir tu propia cuenta");
        var usuario=bloquear(usuarioId);
        var otra=usuarios.buscarPorEmail(email);
        if (otra.isPresent() && !otra.get().id().equals(usuarioId))
            throw new BusinessRuleException("EMAIL_DUPLICADO", "Ese correo ya esta registrado");
        if (!codigo.equals(usuario.codigoInstitucional()) && usuarios.existePorCodigoInstitucional(codigo))
            throw new BusinessRuleException("CODIGO_INSTITUCIONAL_DUPLICADO", "Ese codigo institucional ya esta registrado");
        usuario.actualizarDatosPendientes(email, nombre, perfil, codigo);
        return usuarios.guardar(usuario);
    }
    private Usuario obtener(UsuarioId id) { return usuarios.buscarPorId(id).orElseThrow(() -> new NotFoundException("Usuario", id.value())); }
    private Usuario bloquear(UsuarioId id) { return usuarios.buscarPorIdParaActualizar(id).orElseThrow(() -> new NotFoundException("Usuario", id.value())); }
}
