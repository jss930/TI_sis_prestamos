package com.biblioteca.identity.domain.model;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.shared.domain.BusinessRuleException;
import java.util.EnumSet;
import java.util.Set;

/** Asignaciones administrativas; no sustituyen los permisos operativos de la cuenta. */
public final class ResponsabilidadUsuario {
    private final UsuarioId usuarioId;
    private boolean capacitacionVerificada;
    private final Set<CategoriaResponsable> categorias;

    public ResponsabilidadUsuario(UsuarioId usuarioId, boolean capacitacionVerificada,
                                   Set<CategoriaResponsable> categorias) {
        this.usuarioId = usuarioId;
        this.capacitacionVerificada = capacitacionVerificada;
        this.categorias = categorias.isEmpty() ? EnumSet.noneOf(CategoriaResponsable.class)
                : EnumSet.copyOf(categorias);
    }

    public boolean asignar(CategoriaResponsable categoria, Usuario usuario) {
        exigirValidado(usuario);
        if (categoria.requiereCapacitacion() && !capacitacionVerificada) {
            throw new BusinessRuleException("CAPACITACION_REQUERIDA",
                    "La categoria requiere capacitacion tecnica verificada");
        }
        return categorias.add(categoria);
    }

    public boolean retirar(CategoriaResponsable categoria) {
        return categorias.remove(categoria);
    }

    public void cambiarCapacitacion(boolean verificada, Usuario usuario) {
        if (verificada) exigirValidado(usuario);
        capacitacionVerificada = verificada;
        if (!verificada) categorias.removeIf(CategoriaResponsable::requiereCapacitacion);
    }

    private void exigirValidado(Usuario usuario) {
        if (!usuarioId.equals(usuario.id()) || usuario.estado() != EstadoCuentaUsuario.VALIDADO) {
            throw new BusinessRuleException("USUARIO_NO_VALIDADO",
                    "Solo las cuentas validadas pueden recibir asignaciones o capacitacion");
        }
    }

    public UsuarioId usuarioId() { return usuarioId; }
    public boolean capacitacionVerificada() { return capacitacionVerificada; }
    public Set<CategoriaResponsable> categorias() { return Set.copyOf(categorias); }
}
