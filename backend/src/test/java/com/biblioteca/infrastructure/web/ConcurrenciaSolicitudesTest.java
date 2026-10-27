package com.biblioteca.infrastructure.web;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.identity.domain.model.*;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.inventory.domain.model.*;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.application.command.AceptarReservaCommand;
import com.biblioteca.reservations.application.usecase.*;
import com.biblioteca.reservations.domain.model.*;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.support.Fixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcurrenciaSolicitudesTest {
    @Autowired PlatformTransactionManager transacciones;
    @Autowired UsuarioRepository usuarios;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EjemplarRepository ejemplares;
    @Autowired ReservaRepository reservas;
    @Autowired PrestamoRepository prestamos;
    @Autowired AceptarReservaUseCase aprobar;
    @Autowired EntregarSolicitudUseCase entregar;
    @Autowired EntityManager em;
    @Autowired com.biblioteca.identity.application.usecase.GestionarResponsablesUseCase gestionar;
    @Autowired com.biblioteca.identity.domain.repository.ResponsabilidadRepository responsabilidades;

    record Escenario(Usuario operador, Usuario alumno, Usuario otroAlumno, Ejemplar ejemplar, Reserva primera, Reserva segunda) { }
    Escenario preparar() {
        return new TransactionTemplate(transacciones).execute(tx -> {
            var operador=Fixtures.alumnoValidado();
            for (var permiso : PermisoOperativo.values()) operador.otorgarPermiso(permiso);
            usuarios.guardar(operador);
            var alumno=usuarios.guardar(Fixtures.alumnoValidado());
            var otro=usuarios.guardar(Fixtures.alumnoValidado());
            var item=catalogo.guardar(Fixtures.laptop());
            var ejemplar=ejemplares.guardar(Fixtures.ejemplarDisponible(item.id()));
            var primera=reservas.guardar(Reserva.solicitar(alumno.id(), item.id()));
            var segunda=reservas.guardar(Reserva.solicitar(otro.id(), item.id()));
            return new Escenario(operador, alumno, otro, ejemplar, primera, segunda);
        });
    }
    void limpiar(Escenario e) {
        new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
            em.createNativeQuery("DELETE FROM reservas WHERE id IN (:primera, :segunda)")
                .setParameter("primera",e.primera.id().value()).setParameter("segunda",e.segunda.id().value()).executeUpdate();
            em.createNativeQuery("DELETE FROM ejemplares WHERE id = :id").setParameter("id", e.ejemplar.id().value()).executeUpdate();
            em.createNativeQuery("DELETE FROM catalogo_items WHERE id = :id").setParameter("id", e.ejemplar.catalogoItemId().value()).executeUpdate();
            for (String tabla : java.util.List.of("cargos_responsables", "historial_responsables", "responsabilidades_usuarios")) {
                em.createNativeQuery("DELETE FROM " + tabla + " WHERE usuario_id IN (:a, :b, :c)")
                    .setParameter("a",e.operador.id().value()).setParameter("b",e.alumno.id().value())
                    .setParameter("c",e.otroAlumno.id().value()).executeUpdate();
            }
            em.createNativeQuery("DELETE FROM usuarios WHERE id IN (:a, :b, :c)").setParameter("a",e.operador.id().value())
                .setParameter("b",e.alumno.id().value()).setParameter("c",e.otroAlumno.id().value()).executeUpdate();
        });
    }
    @Test void dosAprobacionesSimultaneasSoloApartanUnaUnidad() throws Exception {
        var e=preparar();
        try (var ejecutor=Executors.newFixedThreadPool(2)) {
            var inicio=new CountDownLatch(1);
            Callable<Boolean> primera=() -> intentar(e,e.primera,inicio);
            Callable<Boolean> segunda=() -> intentar(e,e.segunda,inicio);
            var a=ejecutor.submit(primera); var b=ejecutor.submit(segunda); inicio.countDown();
            assertThat(java.util.List.of(a.get(10,TimeUnit.SECONDS), b.get(10,TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder(true,false);
            new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
                assertThat(ejemplares.buscarPorId(e.ejemplar.id()).orElseThrow().estadoFisico()).isEqualTo(EstadoFisico.RESERVADO);
                assertThat(prestamos.existeActivoPara(e.ejemplar.id())).isFalse();
            });
        } finally { limpiar(e); }
    }
    boolean intentar(Escenario e, Reserva r, CountDownLatch inicio) throws Exception {
        inicio.await();
        try { aprobar.ejecutar(new AceptarReservaCommand(e.operador.id(),r.id(),e.ejemplar.id())); return true; }
        catch (BusinessRuleException ex) { assertThat(ex.codigo()).isEqualTo("EJEMPLAR_NO_DISPONIBLE"); return false; }
    }
    @Test void entregaRechazadaRevierteLaLiberacionDelEjemplar() {
        var e=preparar();
        try {
            aprobar.ejecutar(new AceptarReservaCommand(e.operador.id(),e.primera.id(),e.ejemplar.id()));
            new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
                var alumno=usuarios.buscarPorId(e.alumno.id()).orElseThrow(); alumno.suspender("Regularizacion pendiente"); usuarios.guardar(alumno);
            });
            assertThatThrownBy(() -> entregar.ejecutar(e.operador.id(),e.primera.id())).isInstanceOf(BusinessRuleException.class);
            new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
                assertThat(ejemplares.buscarPorId(e.ejemplar.id()).orElseThrow().estadoFisico()).isEqualTo(EstadoFisico.RESERVADO);
                assertThat(reservas.buscarPorId(e.primera.id()).orElseThrow().estado()).isEqualTo(EstadoReserva.APROBADA);
                assertThat(prestamos.existeActivoPara(e.ejemplar.id())).isFalse();
            });
        } finally { limpiar(e); }
    }
    @Test void dosAdministradoresNoPuedenAsignarElMismoCargoALaVez() throws Exception {
        var e=preparar();
        try (var ejecutor=Executors.newFixedThreadPool(2)) {
            var inicio=new CountDownLatch(1);
            Callable<Boolean> asignarPrimero=() -> intentarAsignacion(e,e.alumno,inicio);
            Callable<Boolean> asignarSegundo=() -> intentarAsignacion(e,e.otroAlumno,inicio);
            var a=ejecutor.submit(asignarPrimero); var b=ejecutor.submit(asignarSegundo); inicio.countDown();
            assertThat(java.util.List.of(a.get(10,TimeUnit.SECONDS), b.get(10,TimeUnit.SECONDS)))
                .containsExactlyInAnyOrder(true,false);
            new TransactionTemplate(transacciones).executeWithoutResult(tx -> {
                assertThat(responsabilidades.listar().stream().filter(r ->
                    r.cargos().get(CategoriaResponsable.DEPORTES) == CargoResponsable.PRINCIPAL).count()).isEqualTo(1);
            });
        } finally { limpiar(e); }
    }
    boolean intentarAsignacion(Escenario e, Usuario candidato, CountDownLatch inicio) throws Exception {
        inicio.await();
        try {
            gestionar.confirmar(e.operador.id(),candidato.id(),false,java.util.Set.of(CategoriaResponsable.DEPORTES),
                java.util.Map.of(CategoriaResponsable.DEPORTES,CargoResponsable.PRINCIPAL));
            return true;
        } catch (BusinessRuleException ex) { assertThat(ex.codigo()).isEqualTo("CARGO_OCUPADO"); return false; }
    }

}
