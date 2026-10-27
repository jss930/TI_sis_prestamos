package com.biblioteca.infrastructure.reservations;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryReservaRepository;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.reservations.domain.model.Reserva;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DisponibilidadReservasAdapterTest {
    @Test
    void bloqueaOtrosPrestamosPeroPermiteLaRecogidaDelTitular() {
        var repositorio = new InMemoryReservaRepository();
        var titular = new UsuarioId(UUID.randomUUID());
        var ejemplar = new EjemplarId(UUID.randomUUID());
        var ahora = Instant.parse("2026-10-27T12:00:00Z");
        var inicio = Instant.parse("2026-10-27T15:00:00Z");
        var fin = Instant.parse("2026-10-27T17:00:00Z");
        Reserva reserva = Reserva.solicitarProgramada(titular, new CatalogoItemId(UUID.randomUUID()),
                LugarUso.EN_CAMPUS, inicio, fin, ahora, ZoneId.of("America/Lima"));
        reserva.aprobarProgramada(new UsuarioId(UUID.randomUUID()), ejemplar,
                Instant.parse("2026-10-27T13:00:00Z"));
        repositorio.guardar(reserva);
        var disponibilidad = new DisponibilidadReservasAdapter(repositorio);

        assertThat(disponibilidad.existeConflicto(ejemplar, new UsuarioId(UUID.randomUUID()),
                inicio, fin)).isTrue();
        assertThat(disponibilidad.existeConflicto(ejemplar, titular,
                inicio.plusSeconds(300), fin)).isFalse();
    }
}
