package com.biblioteca.penalties.domain.policy;

import com.biblioteca.penalties.domain.vo.MontoMulta;

/** Reglas de sancion por retraso. */
public interface PoliticaSancion {

    int diasSuspension(int diasRetraso);

    MontoMulta multaPorRetraso(int diasRetraso);
}
