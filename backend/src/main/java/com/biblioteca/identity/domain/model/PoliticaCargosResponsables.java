package com.biblioteca.identity.domain.model;

import com.biblioteca.shared.domain.BusinessRuleException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PoliticaCargosResponsables {
    private PoliticaCargosResponsables() { }
    public static void exigirCategorias(Set<CategoriaResponsable> categorias, Map<CategoriaResponsable, CargoResponsable> cargos) {
        if (!categorias.equals(cargos.keySet())) throw new BusinessRuleException("CARGOS_INCONSISTENTES", "Selecciona un cargo para cada area asignada");
    }
    public static void validar(ResponsabilidadUsuario candidata, List<ResponsabilidadUsuario> existentes) {
        candidata.cargos().forEach((categoria, cargo) -> {
            boolean ocupado = existentes.stream().filter(r -> !r.usuarioId().equals(candidata.usuarioId()))
                    .anyMatch(r -> r.cargos().get(categoria) == cargo);
            if (ocupado) throw new BusinessRuleException("CARGO_OCUPADO", "El cargo " + cargo + " de " + categoria
                    + " ya esta ocupado. Retira o cambia la asignacion actual antes de reemplazarla");
        });
    }
}
