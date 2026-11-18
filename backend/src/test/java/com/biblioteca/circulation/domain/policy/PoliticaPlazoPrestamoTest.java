package com.biblioteca.circulation.domain.policy;

import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.identity.domain.model.PerfilPrestatario;
import com.biblioteca.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class PoliticaPlazoPrestamoTest {
    private final PoliticaPrestamo politica = new PoliticaPrestamoEstandar();
    private final Instant inicio = Instant.parse("2026-11-17T12:00:00Z"); // 07:00 Lima

    @Test
    void matrizCompletaDeDomicilioYPlazos() {
        for (var perfil : PerfilPrestatario.values()) {
            for (var tipo : TipoItem.values()) {
                boolean permitido = tipo == TipoItem.LIBRO
                        || (tipo == TipoItem.EQUIPO && perfil != PerfilPrestatario.ALUMNO);
                assertThat(politica.permiteDomicilio(perfil, tipo)).isEqualTo(permitido);
                int dias = tipo == TipoItem.LIBRO && perfil == PerfilPrestatario.DOCENTE ? 5 : 3;
                if (permitido) {
                    assertThat(politica.diasPrestamo(perfil, tipo)).isEqualTo(dias);
                    validar(inicio, inicio.plus(Duration.ofDays(dias)), LugarUso.DOMICILIO, perfil, tipo);
                    assertThatThrownBy(() -> validar(inicio, inicio.plus(Duration.ofDays(dias)).plusSeconds(1),
                            LugarUso.DOMICILIO, perfil, tipo)).isInstanceOf(BusinessRuleException.class);
                    validar(inicio, inicio.plus(Duration.ofHours(1)), LugarUso.DOMICILIO, perfil, tipo);
                } else {
                    assertThatThrownBy(() -> validar(inicio, inicio.plus(Duration.ofHours(1)),
                            LugarUso.DOMICILIO, perfil, tipo)).isInstanceOf(BusinessRuleException.class);
                }
            }
        }
    }

    @Test
    void campusSiempreEntreUnaYTreceHorasParaTodosLosPerfilesYBienes() {
        for (var perfil : PerfilPrestatario.values()) {
            for (var tipo : TipoItem.values()) {
                validar(inicio, inicio.plus(Duration.ofHours(13)), LugarUso.EN_CAMPUS, perfil, tipo);
                validar(inicio, inicio.plus(Duration.ofHours(1)), LugarUso.EN_CAMPUS, perfil, tipo);
                assertThatThrownBy(() -> validar(inicio, inicio.plusSeconds(3599), LugarUso.EN_CAMPUS,
                        perfil, tipo)).isInstanceOf(BusinessRuleException.class);
                assertThatThrownBy(() -> validar(inicio, inicio.plus(Duration.ofHours(13)).plusSeconds(1),
                        LugarUso.EN_CAMPUS, perfil, tipo)).isInstanceOf(BusinessRuleException.class);
                assertThatThrownBy(() -> validar(inicio.plus(Duration.ofHours(12)),
                        inicio.plus(Duration.ofDays(1)), LugarUso.EN_CAMPUS, perfil, tipo))
                        .isInstanceOf(BusinessRuleException.class);
            }
        }
    }

    @Test
    void rechazaEntregaAntesDeAbrirYDevolucionDespuesDelCierre() {
        assertThatThrownBy(() -> validar(inicio.minusSeconds(1), inicio.plusSeconds(3600),
                LugarUso.EN_CAMPUS, PerfilPrestatario.ALUMNO, TipoItem.LIBRO))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> validar(inicio.plusSeconds(3600), inicio.plusSeconds(13 * 3600 + 1),
                LugarUso.EN_CAMPUS, PerfilPrestatario.ALUMNO, TipoItem.LIBRO))
                .isInstanceOf(BusinessRuleException.class);
    }

    private void validar(Instant entrega, Instant fin, LugarUso lugar, PerfilPrestatario perfil, TipoItem tipo) {
        PoliticaPlazoPrestamo.validar(entrega, fin, lugar, perfil, tipo, politica);
    }
}
