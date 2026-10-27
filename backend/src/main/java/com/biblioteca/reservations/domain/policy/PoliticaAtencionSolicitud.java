package com.biblioteca.reservations.domain.policy;

import com.biblioteca.catalog.domain.model.*;
import com.biblioteca.identity.domain.model.*;
import com.biblioteca.shared.domain.BusinessRuleException;

public final class PoliticaAtencionSolicitud {
    private PoliticaAtencionSolicitud() { }
    public static boolean puedeAtender(Usuario usuario, ResponsabilidadUsuario responsabilidad, AreaCatalogo area) {
        return usuario.puedeAdministrarSistema() || (usuario.estado() == EstadoCuentaUsuario.VALIDADO
                && responsabilidad != null
                && responsabilidad.categorias().contains(CategoriaResponsable.valueOf(area.name()))
                && (!CategoriaResponsable.valueOf(area.name()).requiereCapacitacion() || responsabilidad.capacitacionVerificada()));
    }
    public static void exigirResponsable(Usuario usuario, ResponsabilidadUsuario responsabilidad, AreaCatalogo area) {
        if (!puedeAtender(usuario, responsabilidad, area))
            throw new BusinessRuleException("AREA_NO_AUTORIZADA", "No tienes una responsabilidad vigente para el area de este bien");
    }
}
