package com.biblioteca.penalties.domain.model;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.domain.policy.PoliticaSancionEstandar;
import com.biblioteca.penalties.domain.vo.SancionId;
import com.biblioteca.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SancionTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 20);

    @Test
    void calculaDiasYMultaPorRetraso() {
        var sancion = Sancion.porRetraso(SancionId.nuevo(), new UsuarioId(UUID.randomUUID()),
                new PrestamoId(UUID.randomUUID()), 3, HOY, new PoliticaSancionEstandar());

        assertThat(sancion.motivo()).isEqualTo(MotivoSancion.RETRASO);
        assertThat(sancion.diasSuspension()).isEqualTo(3);
        assertThat(sancion.multa().monto()).isEqualByComparingTo("3.00");
        assertThat(sancion.multa().moneda()).isEqualTo("PEN");
        assertThat(sancion.fechaFin()).isEqualTo(HOY.plusDays(3));
    }

    @Test
    void estaVigenteDesdeElInicioHastaAntesDelFinExclusivo() {
        var sancion = Sancion.porRetraso(SancionId.nuevo(), new UsuarioId(UUID.randomUUID()),
                new PrestamoId(UUID.randomUUID()), 3, HOY, new PoliticaSancionEstandar());

        assertThat(sancion.estaVigente(HOY)).isTrue();
        assertThat(sancion.estaVigente(HOY.plusDays(2))).isTrue();
        assertThat(sancion.estaVigente(HOY.plusDays(3))).isFalse();
        assertThat(sancion.estaVigente(HOY.minusDays(1))).isFalse();
    }

    @Test
    void cumplirCierraLaSancion() {
        var sancion = sancionSimple();

        sancion.cumplir();

        assertThat(sancion.estado()).isEqualTo(EstadoSancion.CUMPLIDA);
        assertThat(sancion.estaVigente(HOY)).isFalse();
    }

    @Test
    void noSeCumpleDosVeces() {
        var sancion = sancionSimple();
        sancion.cumplir();

        assertThatThrownBy(sancion::cumplir)
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("SANCION_NO_ACTIVA"));
    }

    @Test
    void anularExigeMotivo() {
        var sancion = sancionSimple();

        assertThatThrownBy(() -> sancion.anular(" ")).hasMessageContaining("motivo");

        sancion.anular("Error administrativo");
        assertThat(sancion.estado()).isEqualTo(EstadoSancion.ANULADA);
    }

    @Test
    void laPoliticaTopeaLaSancionEnTreintaDias() {
        var politica = new PoliticaSancionEstandar();

        assertThat(politica.diasSuspension(45)).isEqualTo(30);
        assertThat(politica.multaPorRetraso(45).monto()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(politica.diasSuspension(0)).isZero();
        assertThat(politica.multaPorRetraso(0).monto()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private static Sancion sancionSimple() {
        return Sancion.manual(SancionId.nuevo(), new UsuarioId(UUID.randomUUID()),
                null, MotivoSancion.DANO, 2, new com.biblioteca.penalties.domain.vo.MontoMulta(
                        new BigDecimal("10.00"), "PEN"), HOY, "Dano en tapa");
    }
}
