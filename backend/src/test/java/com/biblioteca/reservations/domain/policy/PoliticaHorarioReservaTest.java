package com.biblioteca.reservations.domain.policy;

import com.biblioteca.shared.domain.BusinessRuleException;
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
                Instant.parse("2026-10-27T15:00:00Z"),
                Instant.parse("2026-10-27T17:00:00Z"), ahora, lima);
    }

    @Test
    void rechazaMenosDeDosHorasDeAnticipacion() {
        assertThatThrownBy(() -> PoliticaHorarioReserva.validar(
                Instant.parse("2026-10-27T13:30:00Z"),
                Instant.parse("2026-10-27T15:00:00Z"), ahora, lima))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("2 horas");
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
