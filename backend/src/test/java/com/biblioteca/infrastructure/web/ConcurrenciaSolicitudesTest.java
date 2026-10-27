package com.biblioteca.infrastructure.web;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.identity.domain.model.PermisoOperativo;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.application.command.AceptarReservaCommand;
import com.biblioteca.reservations.application.usecase.AceptarReservaUseCase;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.support.Fixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrenciaSolicitudesTest {
    @Autowired PlatformTransactionManager transacciones;
    @Autowired UsuarioRepository usuarios;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EjemplarRepository ejemplares;
    @Autowired ReservaRepository reservas;
    @Autowired AceptarReservaUseCase aprobar;
    @Autowired EntityManager em;
    @Autowired Clock clock;

    record Escenario(java.util.UUID operador, java.util.UUID alumnoA, java.util.UUID alumnoB,
                     java.util.UUID item, java.util.UUID ejemplar,
                     java.util.UUID reservaA, java.util.UUID reservaB) { }

    @Test
    void dosAprobacionesSimultaneasDelMismoHorarioSoloAsignanLaUltimaCopiaUnaVez() throws Exception {
        Escenario escenario = preparar();
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            var salida = new CountDownLatch(1);
            Callable<Boolean> primera = () -> intentar(escenario.operador, escenario.reservaA, salida);
            Callable<Boolean> segunda = () -> intentar(escenario.operador, escenario.reservaB, salida);
            var a = ejecutor.submit(primera);
            var b = ejecutor.submit(segunda);
            salida.countDown();

            assertThat(List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
                long aprobadas = List.of(escenario.reservaA, escenario.reservaB).stream()
                        .map(id -> reservas.buscarPorId(new com.biblioteca.reservations.domain.vo.ReservaId(id)).orElseThrow())
                        .filter(r -> r.estado() == EstadoReserva.APROBADA).count();
                assertThat(aprobadas).isEqualTo(1);
                assertThat(ejemplares.buscarPorId(new com.biblioteca.inventory.domain.vo.EjemplarId(escenario.ejemplar))
                        .orElseThrow().estadoFisico()).isEqualTo(EstadoFisico.DISPONIBLE);
            });
        } finally {
            limpiar(escenario);
        }
    }

    private Escenario preparar() {
        return new TransactionTemplate(transacciones).execute(tx -> {
            var operador = Fixtures.alumnoValidado();
            for (var permiso : PermisoOperativo.values()) operador.otorgarPermiso(permiso);
            usuarios.guardar(operador);
            var alumnoA = usuarios.guardar(Fixtures.alumnoValidado());
            var alumnoB = usuarios.guardar(Fixtures.alumnoValidado());
            var item = catalogo.guardar(Fixtures.laptop());
            var ejemplar = ejemplares.guardar(Fixtures.ejemplarDisponible(item.id()));
            var fecha = LocalDate.now(clock).plusDays(1);
            var inicio = fecha.atTime(LocalTime.of(10, 0)).atZone(clock.getZone()).toInstant();
            var fin = fecha.atTime(LocalTime.of(12, 0)).atZone(clock.getZone()).toInstant();
            var primera = reservas.guardar(Reserva.solicitarProgramada(alumnoA.id(), item.id(),
                    LugarUso.EN_CAMPUS, inicio, fin, clock.instant(), clock.getZone()));
            var segunda = reservas.guardar(Reserva.solicitarProgramada(alumnoB.id(), item.id(),
                    LugarUso.EN_CAMPUS, inicio, fin, clock.instant(), clock.getZone()));
            return new Escenario(operador.id().value(), alumnoA.id().value(), alumnoB.id().value(),
                    item.id().value(), ejemplar.id().value(), primera.id().value(), segunda.id().value());
        });
    }

    private boolean intentar(java.util.UUID operador, java.util.UUID reserva, CountDownLatch salida)
            throws InterruptedException {
        salida.await();
        try {
            aprobar.ejecutar(new AceptarReservaCommand(
                    new com.biblioteca.identity.domain.vo.UsuarioId(operador),
                    new com.biblioteca.reservations.domain.vo.ReservaId(reserva)));
            return true;
        } catch (BusinessRuleException ex) {
            assertThat(ex.codigo()).isEqualTo("EJEMPLAR_NO_DISPONIBLE");
            return false;
        }
    }

    private void limpiar(Escenario e) {
        new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
            em.createNativeQuery("DELETE FROM reservas WHERE id IN (:a, :b)")
                    .setParameter("a", e.reservaA).setParameter("b", e.reservaB).executeUpdate();
            em.createNativeQuery("DELETE FROM ejemplares WHERE id = :id").setParameter("id", e.ejemplar).executeUpdate();
            em.createNativeQuery("DELETE FROM catalogo_items WHERE id = :id").setParameter("id", e.item).executeUpdate();
            em.createNativeQuery("DELETE FROM usuarios WHERE id IN (:a, :b, :c)")
                    .setParameter("a", e.operador).setParameter("b", e.alumnoA)
                    .setParameter("c", e.alumnoB).executeUpdate();
        });
    }
}
