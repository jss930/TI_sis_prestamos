package com.biblioteca.circulation.domain.policy;

import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.identity.domain.model.PerfilPrestatario;

public final class PoliticaPrestamoEstandar implements PoliticaPrestamo {

    @Override
    public int diasPrestamo(PerfilPrestatario perfil, TipoItem tipo) {
        return switch (perfil) {
            case ALUMNO -> switch (tipo) {
                case LIBRO -> 3;
                case EQUIPO, OTRO -> 0; // Solo campus.
            };
            case DOCENTE -> switch (tipo) {
                case LIBRO -> 5;
                case EQUIPO -> 3;
                case OTRO -> 0;
            };
            case ADMINISTRATIVO -> switch (tipo) {
                case LIBRO, EQUIPO -> 3;
                case OTRO -> 0;
            };
        };
    }

    @Override
    public boolean permiteDomicilio(PerfilPrestatario perfil, TipoItem tipo) {
        return tipo == TipoItem.LIBRO
                || (tipo == TipoItem.EQUIPO && perfil != PerfilPrestatario.ALUMNO);
    }

    @Override
    public int maxPrestamosSimultaneos(PerfilPrestatario perfil) {
        return switch (perfil) {
            case ALUMNO -> 3;
            case DOCENTE, ADMINISTRATIVO -> 3;
        };
    }

    @Override
    public int maxRenovaciones(PerfilPrestatario perfil) {
        return switch (perfil) {
            case ALUMNO -> 1;
            case DOCENTE, ADMINISTRATIVO -> 2;
        };
    }
}
