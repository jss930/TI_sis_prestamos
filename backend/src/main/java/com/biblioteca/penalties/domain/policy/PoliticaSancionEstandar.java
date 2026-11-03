package com.biblioteca.penalties.domain.policy;

import com.biblioteca.penalties.domain.vo.MontoMulta;

/**
 * Politica base: un dia de suspension por dia de retraso, con tope de siete dias.
 * Las multas por retraso superior al tope se registran manualmente.
 */
public final class PoliticaSancionEstandar implements PoliticaSancion {

    private static final int TOPE_DIAS = 7;

    @Override
    public int diasSuspension(int diasRetraso) {
        if (diasRetraso <= 0) {
            return 0;
        }
        return Math.min(TOPE_DIAS, diasRetraso);
    }

    @Override
    public MontoMulta multaPorRetraso(int diasRetraso) {
        if (diasRetraso <= 0) {
            return MontoMulta.cero();
        }
        return MontoMulta.cero();
    }
}
