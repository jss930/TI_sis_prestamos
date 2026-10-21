package com.biblioteca.circulation.domain.policy;

import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.identity.domain.model.PerfilPrestatario;

/**
 * Reglas configurables de prestamo. La implementacion por defecto vive en el
 * dominio; en la siguiente fase se reemplaza por una lectura de base de datos
 * sin tocar los casos de uso.
 */
public interface PoliticaPrestamo {

    int diasPrestamo(PerfilPrestatario perfil, TipoItem tipo);

    boolean permiteDomicilio(PerfilPrestatario perfil, TipoItem tipo);

    int maxPrestamosSimultaneos(PerfilPrestatario perfil);

    int maxRenovaciones(PerfilPrestatario perfil);
}
