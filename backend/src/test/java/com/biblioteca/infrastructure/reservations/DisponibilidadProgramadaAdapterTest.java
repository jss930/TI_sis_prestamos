package com.biblioteca.infrastructure.reservations;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryPrestamoRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryReservaRepository;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.reservations.domain.model.Reserva;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DisponibilidadProgramadaAdapterTest {

    @Test
    void permiteReservarDespuesDeQueTerminaElPrestamoActual() {
        var prestamos = new InMemoryPrestamoRepository();
        var reservas = new InMemoryReservaRepository();
        var ejemplar = new EjemplarId(UUID.randomUUID());
        var entrega = Instant.parse("2026-10-27T12:00:00Z");
        var vencimiento = Instant.parse("2026-10-27T15:00:00Z");
        prestamos.guardar(prestamo(ejemplar, entrega, vencimiento));
        var disponibilidad = new DisponibilidadProgramadaAdapter(prestamos, reservas);

        assertThat(disponibilidad.estaLibre(ejemplar,
                Instant.parse("2026-10-27T16:00:00Z"),
                Instant.parse("2026-10-27T18:00:00Z"), null)).isTrue();
    }

    @Test
    void rechazaUnPrestamoQueSeCruzaConElHorario() {
        var prestamos = new InMemoryPrestamoRepository();
        var reservas = new InMemoryReservaRepository();
        var ejemplar = new EjemplarId(UUID.randomUUID());
        prestamos.guardar(prestamo(ejemplar,
                Instant.parse("2026-10-27T12:00:00Z"),
                Instant.parse("2026-10-27T17:00:00Z")));
        var disponibilidad = new DisponibilidadProgramadaAdapter(prestamos, reservas);

        assertThat(disponibilidad.estaLibre(ejemplar,
                Instant.parse("2026-10-27T16:00:00Z"),
                Instant.parse("2026-10-27T18:00:00Z"), null)).isFalse();
    }

    @Test
    void rechazaUnaReservaAprobadaQueSeCruzaConElHorario() {
        var prestamos = new InMemoryPrestamoRepository();
        var reservas = new InMemoryReservaRepository();
        var ejemplar = new EjemplarId(UUID.randomUUID());
        var inicio = Instant.parse("2026-10-27T16:00:00Z");
        var fin = Instant.parse("2026-10-27T18:00:00Z");
        var reserva = Reserva.solicitarProgramada(new UsuarioId(UUID.randomUUID()),
                new CatalogoItemId(UUID.randomUUID()), LugarUso.EN_CAMPUS, inicio, fin,
                Instant.parse("2026-10-27T12:00:00Z"), ZoneId.of("America/Lima"));
        reserva.aprobarProgramada(new UsuarioId(UUID.randomUUID()), ejemplar,
                Instant.parse("2026-10-27T13:00:00Z"));
        reservas.guardar(reserva);
        var disponibilidad = new DisponibilidadProgramadaAdapter(prestamos, reservas);

        assertThat(disponibilidad.estaLibre(ejemplar, inicio.plusSeconds(60), fin, null)).isFalse();
    }

    private Prestamo prestamo(EjemplarId ejemplar, Instant entrega, Instant vencimiento) {
        return Prestamo.registrar(new UsuarioId(UUID.randomUUID()), new UsuarioId(UUID.randomUUID()),
                ejemplar, new PeriodoPrestamo(LocalDate.of(2026, 10, 27), LocalDate.of(2026, 10, 27)),
                LugarUso.EN_CAMPUS, entrega, vencimiento);
    }
}
