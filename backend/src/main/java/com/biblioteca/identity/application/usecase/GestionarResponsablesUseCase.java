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

    /** Confirma la seleccion completa en una unica transaccion. */
    public void confirmar(UsuarioId operadorId, UsuarioId usuarioId,
                          boolean capacitada, Set<CategoriaResponsable> categorias) {
        confirmar(operadorId, usuarioId, capacitada, categorias, null);
    }

    public void confirmar(UsuarioId operadorId, UsuarioId usuarioId, boolean capacitada,
                          Set<CategoriaResponsable> categorias, java.util.Map<CategoriaResponsable, CargoResponsable> cargos) {
        exigirGestor(operadorId);
        responsabilidades.bloquearAsignaciones();
        Usuario usuario = bloquearUsuario(usuarioId);
        ResponsabilidadUsuario responsabilidad = obtener(usuarioId);
        var anteriores = responsabilidad.cargos();
        boolean capacitacionAnterior = responsabilidad.capacitacionVerificada();
        var seleccion = new java.util.EnumMap<CategoriaResponsable, CargoResponsable>(CategoriaResponsable.class);
        if (cargos == null) categorias.forEach(c -> seleccion.put(c, anteriores.getOrDefault(c, CargoResponsable.PRINCIPAL)));
        else seleccion.putAll(cargos);
        PoliticaCargosResponsables.exigirCategorias(categorias, seleccion);
        if (capacitacionAnterior != capacitada) responsabilidad.cambiarCapacitacion(capacitada, usuario);
        for (CategoriaResponsable categoria : responsabilidad.categorias()) {
            if (!categorias.contains(categoria)) responsabilidad.retirar(categoria);
        }
        seleccion.forEach((categoria, cargo) -> {
            if (responsabilidad.cargos().get(categoria) != cargo) responsabilidad.asignar(categoria, cargo, usuario);
        });
        guardarCambios(operadorId, usuario, responsabilidad, anteriores, capacitacionAnterior);
    }

    public void asignar(UsuarioId operadorId, UsuarioId usuarioId, CategoriaResponsable categoria, boolean asignada) {
        asignar(operadorId, usuarioId, categoria, asignada, null);
    }

    public void asignar(UsuarioId operadorId, UsuarioId usuarioId, CategoriaResponsable categoria,
                        boolean asignada, CargoResponsable cargo) {
        exigirGestor(operadorId);
        responsabilidades.bloquearAsignaciones();
        Usuario usuario = bloquearUsuario(usuarioId);
        ResponsabilidadUsuario responsabilidad = obtener(usuarioId);
        var anteriores = responsabilidad.cargos();
        if (asignada) responsabilidad.asignar(categoria,
                cargo == null ? anteriores.getOrDefault(categoria, CargoResponsable.PRINCIPAL) : cargo, usuario);
        else responsabilidad.retirar(categoria);
        guardarCambios(operadorId, usuario, responsabilidad, anteriores, responsabilidad.capacitacionVerificada());
    }

    public void capacitar(UsuarioId operadorId, UsuarioId usuarioId, boolean verificada) {
        exigirGestor(operadorId);
        responsabilidades.bloquearAsignaciones();
        Usuario usuario = bloquearUsuario(usuarioId);
        ResponsabilidadUsuario responsabilidad = obtener(usuarioId);
        var anteriores = responsabilidad.cargos();
        boolean capacitacionAnterior = responsabilidad.capacitacionVerificada();
        if (capacitacionAnterior == verificada) return;
        responsabilidad.cambiarCapacitacion(verificada, usuario);
        guardarCambios(operadorId, usuario, responsabilidad, anteriores, capacitacionAnterior);
    }

    private void guardarCambios(UsuarioId operadorId, Usuario usuario, ResponsabilidadUsuario responsabilidad,
            java.util.Map<CategoriaResponsable, CargoResponsable> anteriores, boolean capacitacionAnterior) {
        var actuales = responsabilidad.cargos();
        if (anteriores.equals(actuales) && capacitacionAnterior == responsabilidad.capacitacionVerificada()) return;
        PoliticaCargosResponsables.validar(responsabilidad, responsabilidades.listar());
        sincronizarPermisosOperativos(usuario, responsabilidad);
        responsabilidades.guardar(responsabilidad);
        usuarios.guardar(usuario);
        if (capacitacionAnterior != responsabilidad.capacitacionVerificada()) registrar(usuario.id(), operadorId, null,
                responsabilidad.capacitacionVerificada() ? CambioResponsable.Accion.CAPACITACION_VERIFICADA
                        : CambioResponsable.Accion.CAPACITACION_RETIRADA, null);
        anteriores.keySet().stream().sorted().forEach(c -> {
            if (!actuales.containsKey(c)) registrar(usuario.id(), operadorId, c, CambioResponsable.Accion.RETIRADO, anteriores.get(c));
            else if (actuales.get(c) != anteriores.get(c)) registrar(usuario.id(), operadorId, c, CambioResponsable.Accion.CARGO_CAMBIADO, actuales.get(c));
        });
        actuales.keySet().stream().filter(c -> !anteriores.containsKey(c)).sorted()
                .forEach(c -> registrar(usuario.id(), operadorId, c, CambioResponsable.Accion.ASIGNADO, actuales.get(c)));
    }

    /** Una responsabilidad activa habilita la operación de ventanilla y la recepción de devoluciones. */
    private void sincronizarPermisosOperativos(Usuario usuario, ResponsabilidadUsuario responsabilidad) {
        boolean responsableActivo = usuario.estado() == EstadoCuentaUsuario.VALIDADO
                && !responsabilidad.categorias().isEmpty();
        if (responsableActivo) {
            usuario.otorgarPermiso(PermisoOperativo.GESTIONAR_RESERVAS);
            usuario.otorgarPermiso(PermisoOperativo.REGISTRAR_PRESTAMO);
            usuario.otorgarPermiso(PermisoOperativo.REGISTRAR_DEVOLUCION);
        } else {
            usuario.revocarPermiso(PermisoOperativo.GESTIONAR_RESERVAS);
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
                           CategoriaResponsable categoria, CambioResponsable.Accion accion, CargoResponsable cargo) {
        responsabilidades.registrarCambio(new CambioResponsable(UUID.randomUUID(), usuarioId.value(),
                operadorId.value(), categoria, accion, clock.instant(), cargo));
    }

    public record Resumen(List<ResponsabilidadUsuario> responsables, List<CambioResponsable> historial) { }
}
