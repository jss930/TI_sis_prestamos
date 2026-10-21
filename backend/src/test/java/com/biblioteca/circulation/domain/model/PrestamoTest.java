package com.biblioteca.circulation.domain.model;

import com.biblioteca.circulation.domain.event.PrestamoDevueltoConRetrasoEvent;
import com.biblioteca.circulation.domain.event.PrestamoDevueltoEvent;
import com.biblioteca.circulation.domain.event.PrestamoRegistradoEvent;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrestamoTest {

    private static final UsuarioId OPERADOR = new UsuarioId(UUID.randomUUID());
    private static final UsuarioId PRESTATARIO = new UsuarioId(UUID.randomUUID());
    private static final EjemplarId EJEMPLAR = new EjemplarId(UUID.randomUUID());
    private static final LocalDate INICIO = LocalDate.of(2026, 10, 1);
    private static final LocalDate VENCIMIENTO = LocalDate.of(2026, 10, 10);

    @Test
    void bloqueaElAutoprestamo() {
        assertThatThrownBy(() -> Prestamo.registrar(OPERADOR, OPERADOR, EJEMPLAR,
                new PeriodoPrestamo(INICIO, VENCIMIENTO), LugarUso.EN_CAMPUS))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("AUTOPRESTAMO_NO_PERMITIDO"));
    }

    @Test
    void registraElEventoDeAlta() {
        var prestamo = Prestamo.registrar(OPERADOR, PRESTATARIO, EJEMPLAR,
                new PeriodoPrestamo(INICIO, VENCIMIENTO), LugarUso.EN_CAMPUS);

        assertThat(prestamo.eventosDeDominio())
                .singleElement()
                .isInstanceOf(PrestamoRegistradoEvent.class);
    }

    @Test
    void calculaElRetrasoConVencimientoInclusivo() {
        var prestamo = activo(INICIO, VENCIMIENTO);

        assertThat(prestamo.periodo().diasRetraso(VENCIMIENTO)).isZero();
        assertThat(prestamo.periodo().diasRetraso(VENCIMIENTO.plusDays(3))).isEqualTo(3);
        assertThat(prestamo.estaVencido(VENCIMIENTO)).isFalse();
        assertThat(prestamo.estaVencido(VENCIMIENTO.plusDays(1))).isTrue();
    }

    @Test
    void devolucionATiempoNoGeneraEventoDeRetraso() {
        var prestamo = activo(INICIO, VENCIMIENTO);

        prestamo.registrarDevolucion(VENCIMIENTO.minusDays(1), EstadoFisico.DISPONIBLE);

        assertThat(prestamo.estado()).isEqualTo(EstadoPrestamo.DEVUELTO);
        assertThat(prestamo.fechaDevolucion()).isEqualTo(VENCIMIENTO.minusDays(1));
        assertThat(prestamo.eventosDeDominio())
                .anyMatch(PrestamoDevueltoEvent.class::isInstance)
                .noneMatch(PrestamoDevueltoConRetrasoEvent.class::isInstance);
    }

    @Test
    void devolucionTardiaGeneraEventoConDiasDeRetraso() {
        var prestamo = activo(INICIO, VENCIMIENTO);
        LocalDate devolucion = VENCIMIENTO.plusDays(3);

        prestamo.registrarDevolucion(devolucion, EstadoFisico.DISPONIBLE);

        var evento = prestamo.eventosDeDominio().stream()
                .filter(PrestamoDevueltoConRetrasoEvent.class::isInstance)
                .map(PrestamoDevueltoConRetrasoEvent.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(evento.diasRetraso()).isEqualTo(3);
        assertThat(evento.fechaDevolucion()).isEqualTo(devolucion);
        assertThat(prestamo.estado()).isEqualTo(EstadoPrestamo.DEVUELTO);
    }

    @Test
    void unaDevolucionPerdidaCierraElPrestamoComoPerdido() {
        var prestamo = activo(INICIO, VENCIMIENTO);

        prestamo.registrarDevolucion(VENCIMIENTO, EstadoFisico.PERDIDO);

        assertThat(prestamo.estado()).isEqualTo(EstadoPrestamo.PERDIDO);
    }

    @Test
    void noSePuedeDevolverDosVeces() {
        var prestamo = activo(INICIO, VENCIMIENTO);
        prestamo.registrarDevolucion(VENCIMIENTO, EstadoFisico.DISPONIBLE);

        assertThatThrownBy(() -> prestamo.registrarDevolucion(VENCIMIENTO, EstadoFisico.DISPONIBLE))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("PRESTAMO_NO_ACTIVO"));
    }

    @Test
    void renovarExtiendeElVencimiento() {
        var prestamo = activo(INICIO, VENCIMIENTO);

        prestamo.renovar(INICIO.plusDays(4), 7);

        assertThat(prestamo.periodo().vencimiento()).isEqualTo(VENCIMIENTO.plusDays(7));
        assertThat(prestamo.renovaciones()).isEqualTo(1);
    }

    @Test
    void noSeRenuevaUnPrestamoVencido() {
        var prestamo = activo(INICIO, VENCIMIENTO);

        assertThatThrownBy(() -> prestamo.renovar(VENCIMIENTO.plusDays(1), 7))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("PRESTAMO_VENCIDO"));
    }

    private static Prestamo activo(LocalDate inicio, LocalDate vencimiento) {
        return Prestamo.reconstruir(PrestamoId.nuevo(), OPERADOR, PRESTATARIO, EJEMPLAR,
                new PeriodoPrestamo(inicio, vencimiento), LugarUso.EN_CAMPUS,
                EstadoPrestamo.ACTIVO, null, 0, null, Instant.now());
    }
}
