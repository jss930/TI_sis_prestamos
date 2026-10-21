package com.biblioteca.penalties.domain.policy;

import com.biblioteca.penalties.domain.vo.MontoMulta;

import java.math.BigDecimal;

/**
 * Politica base: un dia de suspension y S/ 1.00 de multa por dia de retraso,
 * con tope de 30 dias. Es deliberadamente simple y configurable despues.
 */
public final class PoliticaSancionEstandar implements PoliticaSancion {

    private static final int TOPE_DIAS = 30;
    private static final BigDecimal MULTA_POR_DIA = BigDecimal.ONE;

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
        int diasCobrados = Math.min(TOPE_DIAS, diasRetraso);
        return MontoMulta.deSoles(MULTA_POR_DIA.multiply(BigDecimal.valueOf(diasCobrados)));
    }
}
