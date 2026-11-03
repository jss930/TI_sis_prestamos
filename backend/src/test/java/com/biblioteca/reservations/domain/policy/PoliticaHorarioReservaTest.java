package com.biblioteca.reservations.domain.policy;

import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.circulation.domain.model.LugarUso;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaHorarioReservaTest {
    private final ZoneId lima = ZoneId.of("America/Lima");
    private final Instant ahora = Instant.parse("2026-10-27T12:00:00Z"); // 07:00 en Lima

    @Test
    void aceptaUnHorarioConAnticipacionDentroDeLaJornada() {
        PoliticaHorarioReserva.validar(
                Instant.parse("2026-10-27T14:30:00Z"),
                Instant.parse("2026-10-27T15:30:00Z"), ahora, lima, LugarUso.EN_CAMPUS);
    }

    @Test
    void rechazaMenosDeTreintaMinutosDeAnticipacion() {
        assertThatThrownBy(() -> PoliticaHorarioReserva.validar(
                Instant.parse("2026-10-27T12:15:00Z"),
                Instant.parse("2026-10-27T13:15:00Z"), ahora, lima, LugarUso.EN_CAMPUS))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("30 minutos");
    }

    @Test
    void rechazaUnaReservaMenorAUnaHora() {
        assertThatThrownBy(() -> PoliticaHorarioReserva.validar(
                Instant.parse("2026-10-27T14:30:00Z"), Instant.parse("2026-10-27T15:15:00Z"),
                ahora, lima, LugarUso.EN_CAMPUS))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("una hora");
    }

    @Test
    void permiteDomicilioPorUnDiaPeroNoMasDe24Horas() {
        PoliticaHorarioReserva.validar(Instant.parse("2026-10-27T15:00:00Z"),
                Instant.parse("2026-10-28T15:00:00Z"), ahora, lima, LugarUso.DOMICILIO);
        assertThatThrownBy(() -> PoliticaHorarioReserva.validar(Instant.parse("2026-10-27T15:00:00Z"),
                Instant.parse("2026-10-28T16:00:00Z"), ahora, lima, LugarUso.DOMICILIO))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("24 horas");
    }

    @Test
    void campusNoPuedeAtraviesarDias() {
        assertThatThrownBy(() -> PoliticaHorarioReserva.validar(Instant.parse("2026-10-28T00:00:00Z"),
                Instant.parse("2026-10-28T13:00:00Z"), ahora, lima, LugarUso.EN_CAMPUS))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void detectaSolapamientoPeroPermiteHorariosConsecutivos() {
        Instant inicio = Instant.parse("2026-10-27T15:00:00Z");
        Instant fin = Instant.parse("2026-10-27T17:00:00Z");
        assertThat(PoliticaHorarioReserva.seSolapan(inicio, fin,
                Instant.parse("2026-10-27T16:00:00Z"), Instant.parse("2026-10-27T18:00:00Z"))).isTrue();
        assertThat(PoliticaHorarioReserva.seSolapan(inicio, fin, fin,
                Instant.parse("2026-10-27T18:00:00Z"))).isFalse();
    }
}
