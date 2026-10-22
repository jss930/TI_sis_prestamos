package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.domain.model.*;
import com.biblioteca.identity.domain.repository.ResponsabilidadRepository;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class GestionarResponsablesUseCase {
    private final UsuarioRepository usuarios;
    private final ResponsabilidadRepository responsabilidades;
    private final Clock clock;

    public GestionarResponsablesUseCase(UsuarioRepository usuarios,
            ResponsabilidadRepository responsabilidades, Clock clock) {
        this.usuarios = usuarios;
        this.responsabilidades = responsabilidades;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Resumen consultar(UsuarioId operadorId) {
        exigirGestor(operadorId);
        return new Resumen(responsabilidades.listar(), responsabilidades.historial());
    }

    public void asignar(UsuarioId operadorId, UsuarioId usuarioId,
                        CategoriaResponsable categoria, boolean asignada) {
        exigirGestor(operadorId);
        Usuario usuario = bloquearUsuario(usuarioId);
        ResponsabilidadUsuario responsabilidad = obtener(usuarioId);
        boolean cambio = asignada ? responsabilidad.asignar(categoria, usuario)
                : responsabilidad.retirar(categoria);
        if (cambio) {
            sincronizarPermisosOperativos(usuario, responsabilidad);
            responsabilidades.guardar(responsabilidad);
            usuarios.guardar(usuario);
            registrar(usuarioId, operadorId, categoria,
                    asignada ? CambioResponsable.Accion.ASIGNADO : CambioResponsable.Accion.RETIRADO);
        }
    }

    public void capacitar(UsuarioId operadorId, UsuarioId usuarioId, boolean verificada) {
        exigirGestor(operadorId);
        Usuario usuario = bloquearUsuario(usuarioId);
        ResponsabilidadUsuario responsabilidad = obtener(usuarioId);
        if (responsabilidad.capacitacionVerificada() == verificada) return;
        Set<CategoriaResponsable> anteriores = responsabilidad.categorias();
        responsabilidad.cambiarCapacitacion(verificada, usuario);
        sincronizarPermisosOperativos(usuario, responsabilidad);
        responsabilidades.guardar(responsabilidad);
        usuarios.guardar(usuario);
        registrar(usuarioId, operadorId, null, verificada
                ? CambioResponsable.Accion.CAPACITACION_VERIFICADA
                : CambioResponsable.Accion.CAPACITACION_RETIRADA);
        anteriores.stream().filter(categoria -> !responsabilidad.categorias().contains(categoria))
                .sorted().forEach(categoria -> registrar(usuarioId, operadorId, categoria,
                        CambioResponsable.Accion.RETIRADO));
    }

    /** Una responsabilidad activa habilita la operación de ventanilla y la recepción de devoluciones. */
    private void sincronizarPermisosOperativos(Usuario usuario, ResponsabilidadUsuario responsabilidad) {
        boolean responsableActivo = usuario.estado() == EstadoCuentaUsuario.VALIDADO
                && !responsabilidad.categorias().isEmpty();
        if (responsableActivo) {
            usuario.otorgarPermiso(PermisoOperativo.REGISTRAR_PRESTAMO);
            usuario.otorgarPermiso(PermisoOperativo.REGISTRAR_DEVOLUCION);
        } else {
            usuario.revocarPermiso(PermisoOperativo.REGISTRAR_PRESTAMO);
            usuario.revocarPermiso(PermisoOperativo.REGISTRAR_DEVOLUCION);
        }
    }

    private void exigirGestor(UsuarioId id) {
        Usuario usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Usuario", id.value()));
        if (!usuario.puedeAdministrarSistema()) {
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO",
                    "Se requiere la cuenta administradora del sistema");
        }
    }

    private Usuario bloquearUsuario(UsuarioId id) {
        return usuarios.buscarPorIdParaActualizar(id)
                .orElseThrow(() -> new NotFoundException("Usuario", id.value()));
    }

    private ResponsabilidadUsuario obtener(UsuarioId id) {
        return responsabilidades.buscar(id).orElseGet(() -> new ResponsabilidadUsuario(id, false, Set.of()));
    }

    private void registrar(UsuarioId usuarioId, UsuarioId operadorId,
                           CategoriaResponsable categoria, CambioResponsable.Accion accion) {
        responsabilidades.registrarCambio(new CambioResponsable(UUID.randomUUID(), usuarioId.value(),
                operadorId.value(), categoria, accion, clock.instant()));
    }

    public record Resumen(List<ResponsabilidadUsuario> responsables, List<CambioResponsable> historial) { }
}
