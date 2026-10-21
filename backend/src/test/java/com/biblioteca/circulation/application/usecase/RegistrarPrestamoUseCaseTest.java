package com.biblioteca.circulation.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.application.command.RegistrarPrestamoCommand;
import com.biblioteca.circulation.domain.event.PrestamoRegistradoEvent;
import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.policy.PoliticaPrestamoEstandar;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryCatalogoItemRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryEjemplarRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryPrestamoRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemorySancionRepository;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryUsuarioRepository;
import com.biblioteca.inventory.domain.event.EjemplarPrestadoEvent;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.policy.PoliticaSancionEstandar;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.penalties.domain.vo.SancionId;
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

class RegistrarPrestamoUseCaseTest {

    private UsuarioRepository usuarios;
    private CatalogoItemRepository catalogo;
    private EjemplarRepository ejemplares;
    private PrestamoRepository prestamos;
    private SancionRepository sanciones;
    private RecordingEventPublisher eventos;
    private RegistrarPrestamoUseCase useCase;

    private Usuario bibliotecario;
    private Usuario alumno;
    private CatalogoItem libro;
    private Ejemplar ejemplar;

    @BeforeEach
    void setUp() {
        usuarios = new InMemoryUsuarioRepository();
        catalogo = new InMemoryCatalogoItemRepository();
        ejemplares = new InMemoryEjemplarRepository();
        prestamos = new InMemoryPrestamoRepository();
        sanciones = new InMemorySancionRepository();
        eventos = new RecordingEventPublisher();
        useCase = new RegistrarPrestamoUseCase(usuarios, catalogo, ejemplares, prestamos,
                sanciones, new PoliticaPrestamoEstandar(), eventos, Fixtures.clock());

        bibliotecario = Fixtures.bibliotecario();
        alumno = Fixtures.alumnoValidado();
        libro = Fixtures.libro();
        ejemplar = Fixtures.ejemplarDisponible(libro.id());

        usuarios.guardar(bibliotecario);
        usuarios.guardar(alumno);
        catalogo.guardar(libro);
        ejemplares.guardar(ejemplar);
    }

    @Test
    void registraElPrestamoYReservaElEjemplar() {
        Prestamo prestamo = useCase.ejecutar(command(bibliotecario.id(), alumno.id(), ejemplar.id()));

        assertThat(prestamo.estado()).isEqualTo(EstadoPrestamo.ACTIVO);
        assertThat(prestamo.periodo().inicio()).isEqualTo(Fixtures.HOY);
        assertThat(prestamo.periodo().vencimiento()).isEqualTo(Fixtures.HOY.plusDays(7));
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estadoFisico())
                .isEqualTo(EstadoFisico.PRESTADO);
        assertThat(eventos.contiene(PrestamoRegistradoEvent.class)).isTrue();
        assertThat(eventos.contiene(EjemplarPrestadoEvent.class)).isTrue();
    }

    @Test
    void rechazaUnaCuentaProvisional() {
        Usuario provisional = Fixtures.alumnoProvisional();
        usuarios.guardar(provisional);

        assertThatThrownBy(() -> useCase.ejecutar(
                command(bibliotecario.id(), provisional.id(), ejemplar.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("USUARIO_NO_HABILITADO"));
    }

    @Test
    void rechazaUnOperadorSinPermisoDeVentanilla() {
        assertThatThrownBy(() -> useCase.ejecutar(
                command(alumno.id(), bibliotecario.id(), ejemplar.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("OPERADOR_NO_HABILITADO"));
    }

    @Test
    void rechazaElAutoprestamo() {
        assertThatThrownBy(() -> useCase.ejecutar(
                command(bibliotecario.id(), bibliotecario.id(), ejemplar.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("AUTOPRESTAMO_NO_PERMITIDO"));
    }

    @Test
    void rechazaUnEjemplarNoDisponible() {
        ejemplar.prestar();
        ejemplares.guardar(ejemplar);

        assertThatThrownBy(() -> useCase.ejecutar(
                command(bibliotecario.id(), alumno.id(), ejemplar.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("EJEMPLAR_NO_DISPONIBLE"));
    }

    @Test
    void rechazaUnaSancionActiva() {
        Sancion sancion = Sancion.porRetraso(SancionId.nuevo(), alumno.id(),
                PrestamoId.nuevo(), 2, Fixtures.HOY, new PoliticaSancionEstandar());
        sanciones.guardar(sancion);

        assertThatThrownBy(() -> useCase.ejecutar(
                command(bibliotecario.id(), alumno.id(), ejemplar.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("SANCION_ACTIVA"));
    }

    @Test
    void rechazaUnPrestamoVencidoPendiente() {
        prestamos.guardar(prestamoDe(alumno.id(), Fixtures.HOY.minusDays(10), Fixtures.HOY.minusDays(2)));

        assertThatThrownBy(() -> useCase.ejecutar(
                command(bibliotecario.id(), alumno.id(), ejemplar.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("PRESTAMO_VENCIDO_PENDIENTE"));
    }

    @Test
    void rechazaSuperarElMaximoDePrestamosSimultaneos() {
        for (int i = 0; i < 3; i++) {
            prestamos.guardar(prestamoDe(alumno.id(), Fixtures.HOY, Fixtures.HOY.plusDays(5)));
        }

        assertThatThrownBy(() -> useCase.ejecutar(
                command(bibliotecario.id(), alumno.id(), ejemplar.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("LIMITE_PRESTAMOS"));
    }

    @Test
    void unAlumnoNoPuedeLlevarEquiposADomicilio() {
        CatalogoItem laptop = Fixtures.laptop();
        Ejemplar ejemplarLaptop = Fixtures.ejemplarDisponible(laptop.id());
        catalogo.guardar(laptop);
        ejemplares.guardar(ejemplarLaptop);

        assertThatThrownBy(() -> useCase.ejecutar(new RegistrarPrestamoCommand(
                bibliotecario.id(), alumno.id(), ejemplarLaptop.id(), LugarUso.DOMICILIO)))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("DOMICILIO_NO_PERMITIDO"));
    }

    @Test
    void unDocenteSiPuedeLlevarEquiposADomicilio() {
        Usuario docente = Fixtures.docenteValidado();
        usuarios.guardar(docente);
        CatalogoItem laptop = Fixtures.laptop();
        Ejemplar ejemplarLaptop = Fixtures.ejemplarDisponible(laptop.id());
        catalogo.guardar(laptop);
        ejemplares.guardar(ejemplarLaptop);

        Prestamo prestamo = useCase.ejecutar(new RegistrarPrestamoCommand(
                bibliotecario.id(), docente.id(), ejemplarLaptop.id(), LugarUso.DOMICILIO));

        assertThat(prestamo.periodo().vencimiento()).isEqualTo(Fixtures.HOY.plusDays(7));
    }

    private RegistrarPrestamoCommand command(UsuarioId operador, UsuarioId prestatario, EjemplarId id) {
        return new RegistrarPrestamoCommand(operador, prestatario, id, LugarUso.EN_CAMPUS);
    }

    private static Prestamo prestamoDe(UsuarioId prestatario, LocalDate inicio, LocalDate fin) {
        return Prestamo.reconstruir(PrestamoId.nuevo(), new UsuarioId(UUID.randomUUID()),
                prestatario, new EjemplarId(UUID.randomUUID()),
                new PeriodoPrestamo(inicio, fin), LugarUso.EN_CAMPUS,
                EstadoPrestamo.ACTIVO, null, 0, null, Instant.now());
    }
}
