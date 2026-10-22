package com.biblioteca.reservations.domain.policy;

import com.biblioteca.identity.domain.model.PerfilPrestatario;

public interface PoliticaReserva {

    int maxReservasActivas(PerfilPrestatario perfil);
}
