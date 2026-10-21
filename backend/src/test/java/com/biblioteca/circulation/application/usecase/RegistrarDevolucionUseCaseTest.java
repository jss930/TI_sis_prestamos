package com.biblioteca.circulation.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.circulation.application.command.RegistrarDevolucionCommand;
import com.biblioteca.circulation.domain.event.PrestamoDevueltoConRetrasoEvent;
import com.biblioteca.circulation.domain.event.PrestamoDevueltoEvent;
import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryEjemplarRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryPrestamoRepository;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.support.Fixtures;
import com.biblioteca.support.RecordingEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrarDevolucionUseCaseTest {

    private PrestamoRepository prestamos;
    private EjemplarRepository ejemplares;
    private RecordingEventPublisher eventos;
    private RegistrarDevolucionUseCase useCase;

    private CatalogoItem libro;
    private Ejemplar ejemplar;

    @BeforeEach
    void setUp() {
        prestamos = new InMemoryPrestamoRepository();
        ejemplares = new InMemoryEjemplarRepository();
        eventos = new RecordingEventPublisher();
        useCase = new RegistrarDevolucionUseCase(prestamos, ejemplares, eventos,
                Fixtures.clockEn(Fixtures.HOY));

        libro = Fixtures.libro();
        ejemplar = Fixtures.ejemplarDisponible(libro.id());
        ejemplares.guardar(ejemplar);
    }

    @Test
    void devolucionATiempoLiberaElEjemplar() {
        Prestamo prestamo = prestamoActivo(Fixtures.HOY.minusDays(5), Fixtures.HOY.plusDays(5));

        useCase.ejecutar(new RegistrarDevolucionCommand(prestamo.id(), EstadoFisico.DISPONIBLE));

        assertThat(prestamo.estado()).isEqualTo(EstadoPrestamo.DEVUELTO);
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estadoFisico())
                .isEqualTo(EstadoFisico.DISPONIBLE);
        assertThat(eventos.contiene(PrestamoDevueltoEvent.class)).isTrue();
        assertThat(eventos.contiene(PrestamoDevueltoConRetrasoEvent.class)).isFalse();
    }

    @Test
    void devolucionTardiaPublicaElEventoDeRetraso() {
        Prestamo prestamo = prestamoActivo(Fixtures.HOY.minusDays(19), Fixtures.HOY.minusDays(10));

        useCase.ejecutar(new RegistrarDevolucionCommand(prestamo.id(), EstadoFisico.DISPONIBLE));

        var evento = eventos.deTipo(PrestamoDevueltoConRetrasoEvent.class).getFirst();
        assertThat(evento.diasRetraso()).isEqualTo(10);
        assertThat(evento.prestatarioId()).isEqualTo(prestamo.prestatarioId());
    }

    @Test
    void devolucionPerdidaCierraElPrestamoYElEjemplar() {
        Prestamo prestamo = prestamoActivo(Fixtures.HOY.minusDays(2), Fixtures.HOY.plusDays(5));

        useCase.ejecutar(new RegistrarDevolucionCommand(prestamo.id(), EstadoFisico.PERDIDO));

        assertThat(prestamo.estado()).isEqualTo(EstadoPrestamo.PERDIDO);
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estadoFisico())
                .isEqualTo(EstadoFisico.PERDIDO);
    }

    @Test
    void noSePuedeDevolverDosVeces() {
        Prestamo prestamo = prestamoActivo(Fixtures.HOY.minusDays(2), Fixtures.HOY.plusDays(5));
        useCase.ejecutar(new RegistrarDevolucionCommand(prestamo.id(), EstadoFisico.DISPONIBLE));

        assertThatThrownBy(() -> useCase.ejecutar(
                new RegistrarDevolucionCommand(prestamo.id(), EstadoFisico.DISPONIBLE)))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("PRESTAMO_NO_ACTIVO"));
    }

    private Prestamo prestamoActivo(LocalDate inicio, LocalDate fin) {
        Prestamo prestamo = Prestamo.reconstruir(PrestamoId.nuevo(),
                new UsuarioId(UUID.randomUUID()), new UsuarioId(UUID.randomUUID()),
                ejemplar.id(), new PeriodoPrestamo(inicio, fin), LugarUso.EN_CAMPUS,
                EstadoPrestamo.ACTIVO, null, 0, null, Instant.now());
        ejemplar.prestar();
        ejemplares.guardar(ejemplar);
        prestamos.guardar(prestamo);
        eventos.limpiar();
        return prestamo;
    }
}
