package com.biblioteca.identity.domain.model;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.shared.domain.BusinessRuleException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Asignaciones administrativas; no sustituyen los permisos operativos de la cuenta. */
public final class ResponsabilidadUsuario {
    private final UsuarioId usuarioId;
    private boolean capacitacionVerificada;
    private final Map<CategoriaResponsable, CargoResponsable> cargos;

    public ResponsabilidadUsuario(UsuarioId usuarioId, boolean capacitacionVerificada,
                                   Set<CategoriaResponsable> categorias) {
        this.usuarioId = usuarioId;
        this.capacitacionVerificada = capacitacionVerificada;
        this.cargos = new EnumMap<>(CategoriaResponsable.class);
        categorias.forEach(c -> this.cargos.put(c, CargoResponsable.PRINCIPAL));
    }

    public static ResponsabilidadUsuario reconstruir(UsuarioId usuarioId, boolean capacitada,
                                                     Map<CategoriaResponsable, CargoResponsable> cargos) {
        var responsabilidad = new ResponsabilidadUsuario(usuarioId, capacitada, Set.of());
        cargos.forEach((categoria, cargo) -> responsabilidad.cargos.put(Objects.requireNonNull(categoria), Objects.requireNonNull(cargo)));
        return responsabilidad;
    }

    public boolean asignar(CategoriaResponsable categoria, Usuario usuario) {
        return asignar(categoria, cargos.getOrDefault(categoria, CargoResponsable.PRINCIPAL), usuario);
    }

    public boolean asignar(CategoriaResponsable categoria, CargoResponsable cargo, Usuario usuario) {
        exigirValidado(usuario);
        if (categoria.requiereCapacitacion() && !capacitacionVerificada) {
            throw new BusinessRuleException("CAPACITACION_REQUERIDA",
                    "La categoria requiere capacitacion tecnica verificada");
        }
        return cargos.put(Objects.requireNonNull(categoria), Objects.requireNonNull(cargo)) != cargo;
    }

    public boolean retirar(CategoriaResponsable categoria) {
        return cargos.remove(categoria) != null;
    }

    public void cambiarCapacitacion(boolean verificada, Usuario usuario) {
        if (verificada) exigirValidado(usuario);
        capacitacionVerificada = verificada;
        if (!verificada) cargos.keySet().removeIf(CategoriaResponsable::requiereCapacitacion);
    }

    private void exigirValidado(Usuario usuario) {
        if (!usuarioId.equals(usuario.id()) || usuario.estado() != EstadoCuentaUsuario.VALIDADO) {
            throw new BusinessRuleException("USUARIO_NO_VALIDADO",
                    "Solo las cuentas validadas pueden recibir asignaciones o capacitacion");
        }
    }

    public UsuarioId usuarioId() { return usuarioId; }
    public boolean capacitacionVerificada() { return capacitacionVerificada; }
    public Set<CategoriaResponsable> categorias() { return Set.copyOf(cargos.keySet()); }
    public Map<CategoriaResponsable, CargoResponsable> cargos() { return Map.copyOf(cargos); }
}
