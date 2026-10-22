package com.biblioteca.reservations.domain.policy;

import com.biblioteca.identity.domain.model.PerfilPrestatario;

public final class PoliticaReservaEstandar implements PoliticaReserva {

    @Override
    public int maxReservasActivas(PerfilPrestatario perfil) {
        return switch (perfil) {
            case ALUMNO -> 3;
            case DOCENTE -> 10;
            case ADMINISTRATIVO -> 5;
        };
    }
}
