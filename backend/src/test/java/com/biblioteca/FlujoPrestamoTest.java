package com.biblioteca;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.circulation.application.command.RegistrarDevolucionCommand;
import com.biblioteca.circulation.application.command.RegistrarPrestamoCommand;
import com.biblioteca.circulation.application.usecase.RegistrarDevolucionUseCase;
import com.biblioteca.circulation.application.usecase.RegistrarPrestamoUseCase;
import com.biblioteca.circulation.domain.event.PrestamoDevueltoConRetrasoEvent;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.policy.PoliticaPrestamoEstandar;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryCatalogoItemRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryEjemplarRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryPrestamoRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemorySancionRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryUsuarioRepository;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.penalties.application.listener.PrestamoDevueltoConRetrasoListener;
import com.biblioteca.penalties.domain.event.SancionRegistradaEvent;
import com.biblioteca.penalties.domain.policy.PoliticaSancionEstandar;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainEvent;
import com.biblioteca.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Flujo completo sin Spring: prestamo -> devolucion tardia -> evento ->
 * sancion automatica -> bloqueo del siguiente prestamo.
 */
class FlujoPrestamoTest {

    @Test
    void unPrestamoDevueltoTardeGeneraSancionYBloqueaElSiguiente() {
        var usuarios = new InMemoryUsuarioRepository();
        var catalogo = new InMemoryCatalogoItemRepository();
        var ejemplares = new InMemoryEjemplarRepository();
        var prestamos = new InMemoryPrestamoRepository();
        var sanciones = new InMemorySancionRepository();
        List<DomainEvent> eventos = new ArrayList<>();

        AtomicReference<PrestamoDevueltoConRetrasoListener> listenerRef = new AtomicReference<>();
        DomainEventPublisher publisher = evento -> {
            eventos.add(evento);
            if (evento instanceof PrestamoDevueltoConRetrasoEvent retraso) {
                listenerRef.get().alDevolverConRetraso(retraso);
            }
        };
        var listener = new PrestamoDevueltoConRetrasoListener(
                sanciones, new PoliticaSancionEstandar(), publisher);
        listenerRef.set(listener);

        var politica = new PoliticaPrestamoEstandar();
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate devolucion = LocalDate.of(2026, 10, 20);

        var registrarInicio = new RegistrarPrestamoUseCase(usuarios, catalogo, ejemplares,
                prestamos, sanciones, politica, publisher, Fixtures.clockEn(inicio));
        var registrarDevolucion = new RegistrarDevolucionUseCase(
                prestamos, ejemplares, publisher, Fixtures.clockEn(devolucion));
        var registrarDespues = new RegistrarPrestamoUseCase(usuarios, catalogo, ejemplares,
                prestamos, sanciones, politica, publisher, Fixtures.clockEn(devolucion));

        Usuario bibliotecario = Fixtures.bibliotecario();
        Usuario alumno = Fixtures.alumnoValidado();
        CatalogoItem libro = Fixtures.libro();
        Ejemplar copia1 = Fixtures.ejemplarDisponible(libro.id());
        Ejemplar copia2 = Fixtures.ejemplarDisponible(libro.id());

        usuarios.guardar(bibliotecario);
        usuarios.guardar(alumno);
        catalogo.guardar(libro);
        ejemplares.guardar(copia1);
        ejemplares.guardar(copia2);

        Prestamo prestamo = registrarInicio.ejecutar(new RegistrarPrestamoCommand(
                bibliotecario.id(), alumno.id(), copia1.id(), LugarUso.EN_CAMPUS));

        assertThat(prestamo.periodo().vencimiento()).isEqualTo(inicio.plusDays(7));
        assertThat(ejemplares.buscarPorId(copia1.id()).orElseThrow().estadoFisico())
                .isEqualTo(EstadoFisico.PRESTADO);

        registrarDevolucion.ejecutar(new RegistrarDevolucionCommand(
                prestamo.id(), EstadoFisico.DISPONIBLE));

        assertThat(prestamo.estado()).isEqualTo(
                com.biblioteca.circulation.domain.model.EstadoPrestamo.DEVUELTO);
        assertThat(ejemplares.buscarPorId(copia1.id()).orElseThrow().estadoFisico())
                .isEqualTo(EstadoFisico.DISPONIBLE);

        assertThat(sanciones.listarPorUsuario(alumno.id())).hasSize(1);
        var sancion = sanciones.listarPorUsuario(alumno.id()).getFirst();
        assertThat(sancion.diasSuspension()).isEqualTo(12);
        assertThat(sancion.multa().monto()).isEqualByComparingTo("12.00");
        assertThat(sancion.estaVigente(devolucion)).isTrue();
        assertThat(eventos.stream().anyMatch(SancionRegistradaEvent.class::isInstance)).isTrue();

        assertThatThrownBy(() -> registrarDespues.ejecutar(new RegistrarPrestamoCommand(
                bibliotecario.id(), alumno.id(), copia2.id(), LugarUso.EN_CAMPUS)))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("SANCION_ACTIVA"));
    }
}
