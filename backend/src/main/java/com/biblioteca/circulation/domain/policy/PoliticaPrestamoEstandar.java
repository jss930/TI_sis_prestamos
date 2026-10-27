package com.biblioteca.circulation.domain.policy;

import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.identity.domain.model.PerfilPrestatario;

public final class PoliticaPrestamoEstandar implements PoliticaPrestamo {

    @Override
    public int diasPrestamo(PerfilPrestatario perfil, TipoItem tipo) {
        return switch (perfil) {
            case ALUMNO -> switch (tipo) {
                case LIBRO -> 7;
                case EQUIPO -> 1;
                case OTRO -> 3;
            };
            case DOCENTE -> switch (tipo) {
                case LIBRO -> 30;
                case EQUIPO -> 7;
                case OTRO -> 7;
            };
            case ADMINISTRATIVO -> switch (tipo) {
                case LIBRO -> 15;
                case EQUIPO -> 3;
                case OTRO -> 5;
            };
        };
    }

    @Override
    public boolean permiteDomicilio(PerfilPrestatario perfil, TipoItem tipo) {
        return tipo == TipoItem.LIBRO;
    }

    @Override
    public int maxPrestamosSimultaneos(PerfilPrestatario perfil) {
        return switch (perfil) {
            case ALUMNO -> 3;
            case DOCENTE -> 10;
            case ADMINISTRATIVO -> 5;
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
