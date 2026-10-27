package com.biblioteca.reservations.domain.model;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaTest {

    @Test
    void solicitaYCancela() {
        UsuarioId prestatario = new UsuarioId(UUID.randomUUID());
        Reserva reserva = Reserva.solicitar(prestatario, new CatalogoItemId(UUID.randomUUID()));
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.SOLICITADA);
        reserva.cancelar(prestatario);
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void soloPrestatarioCancela() {
        UsuarioId prestatario = new UsuarioId(UUID.randomUUID());
        Reserva reserva = Reserva.solicitar(prestatario, new CatalogoItemId(UUID.randomUUID()));
        assertThatThrownBy(() -> reserva.cancelar(new UsuarioId(UUID.randomUUID())))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void aceptaConPrestamo() {
        UsuarioId prestatario = new UsuarioId(UUID.randomUUID());
        UsuarioId operador = new UsuarioId(UUID.randomUUID());
        Reserva reserva = Reserva.solicitar(prestatario, new CatalogoItemId(UUID.randomUUID()));
        reserva.aceptar(operador, new PrestamoId(UUID.randomUUID()));
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.ACEPTADA);
        assertThat(reserva.operadorId()).isEqualTo(operador);
    }

    @Test
    void deniegaConMotivo() {
        UsuarioId prestatario = new UsuarioId(UUID.randomUUID());
        UsuarioId operador = new UsuarioId(UUID.randomUUID());
        Reserva reserva = Reserva.solicitar(prestatario, new CatalogoItemId(UUID.randomUUID()));
        reserva.denegar(operador, "sin stock");
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.DENEGADA);
        assertThat(reserva.motivo()).isEqualTo("sin stock");
    }
    @Test
    void alLlegarAlLimiteDeRetiroYaNoSePuedeEntregar() {
        var alumno = new UsuarioId(UUID.randomUUID());
        var operador = new UsuarioId(UUID.randomUUID());
        var ahora = java.time.Instant.parse("2026-10-26T12:00:00Z");
        var limite = ahora.plusSeconds(172800);
        var reserva = Reserva.solicitar(alumno, new CatalogoItemId(UUID.randomUUID()));
        reserva.aprobar(operador, new com.biblioteca.inventory.domain.vo.EjemplarId(UUID.randomUUID()), ahora, limite);
        assertThat(reserva.expirar(limite.minusNanos(1))).isFalse();
        assertThatThrownBy(() -> reserva.exigirRetiroVigente(limite)).isInstanceOf(BusinessRuleException.class);
        assertThat(reserva.expirar(limite)).isTrue();
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.EXPIRADA);
        assertThat(reserva.prestamoId()).isNull();
    }

    @Test
    void cancelarAprobadaConservaQuienYCuandoAprobo() {
        var alumno = new UsuarioId(UUID.randomUUID());
        var operador = new UsuarioId(UUID.randomUUID());
        var ahora = java.time.Instant.parse("2026-10-26T12:00:00Z");
        var reserva = Reserva.solicitar(alumno, new CatalogoItemId(UUID.randomUUID()));
        reserva.aprobar(operador, new com.biblioteca.inventory.domain.vo.EjemplarId(UUID.randomUUID()), ahora, ahora.plusSeconds(172800));
        reserva.cancelar(alumno, ahora.plusSeconds(300));
        assertThat(reserva.operadorId()).isEqualTo(operador);
        assertThat(reserva.fechaDecision()).isEqualTo(ahora);
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.CANCELADA);
    }

}
