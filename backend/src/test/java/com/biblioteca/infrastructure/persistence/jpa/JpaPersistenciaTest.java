package com.biblioteca.infrastructure.persistence.jpa;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.penalties.domain.model.MotivoSancion;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.penalties.domain.vo.MontoMulta;
import com.biblioteca.penalties.domain.vo.SancionId;
import com.biblioteca.support.Fixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que los cinco adaptadores JPA reconstruyan los agregados y que
 * Flyway haya creado un esquema compatible con las entidades.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class JpaPersistenciaTest {

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private CatalogoItemRepository catalogo;

    @Autowired
    private EjemplarRepository ejemplares;

    @Autowired
    private PrestamoRepository prestamos;

    @Autowired
    private SancionRepository sanciones;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persisteYReconstruyeLosCincoContextos() {
        Usuario bibliotecario = Fixtures.bibliotecario();
        Usuario alumno = Fixtures.alumnoValidado();
        CatalogoItem libro = Fixtures.libro();
        Ejemplar ejemplar = Fixtures.ejemplarDisponible(libro.id());

        usuarios.guardar(bibliotecario);
        usuarios.guardar(alumno);
        catalogo.guardar(libro);
        ejemplares.guardar(ejemplar);

        Prestamo prestamo = Prestamo.registrar(bibliotecario.id(), alumno.id(), ejemplar.id(),
                new PeriodoPrestamo(Fixtures.HOY, Fixtures.HOY.plusDays(7)), LugarUso.EN_CAMPUS);
        prestamos.guardar(prestamo);

        Sancion sancion = Sancion.manual(SancionId.nuevo(), alumno.id(), prestamo.id(),
                MotivoSancion.RETRASO, 2, MontoMulta.deSoles(new BigDecimal("2.00")),
                Fixtures.HOY, "Sancion de prueba");
        sanciones.guardar(sancion);

        entityManager.flush();
        entityManager.clear();

        assertThat(usuarios.buscarPorId(alumno.id()).orElseThrow().estado().name())
                .isEqualTo("VALIDADO");
        assertThat(usuarios.buscarPorEmail(alumno.email())).isPresent();
        assertThat(catalogo.buscarPorId(libro.id()).orElseThrow().titulo().value())
                .isEqualTo("Clean Code");
        assertThat(catalogo.existePorIsbn(
                ((com.biblioteca.catalog.domain.model.LibroDetalle) libro.detalle()).isbn())).isTrue();
        assertThat(ejemplares.buscarPorBarcode(ejemplar.barcode())).isPresent();
        assertThat(prestamos.buscarPorId(prestamo.id())).isPresent();
        assertThat(prestamos.contarActivosPor(alumno.id())).isEqualTo(1);
        assertThat(sanciones.buscarPorId(sancion.id())).isPresent();
        assertThat(sanciones.existeActivaPara(alumno.id(), Fixtures.HOY)).isTrue();
        assertThat(sanciones.existePorPrestamo(prestamo.id())).isTrue();
    }
}
