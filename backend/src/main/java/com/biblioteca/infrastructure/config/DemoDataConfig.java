package com.biblioteca.infrastructure.config;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.model.EquipoDetalle;
import com.biblioteca.catalog.domain.model.LibroDetalle;
import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.Autor;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.ISBN;
import com.biblioteca.catalog.domain.vo.Titulo;
import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.model.EstadoCuentaUsuario;
import com.biblioteca.identity.domain.model.PerfilPrestatario;
import com.biblioteca.identity.domain.model.PermisoOperativo;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.NombreCompleto;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.model.Ubicacion;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Datos de demostracion con IDs fijos para poder copiar y pegar los curl.
 * Solo se cargan con el perfil "demo" (el perfil por defecto).
 */
@Configuration
@Profile("demo")
public class DemoDataConfig {

    private static final Logger log = LoggerFactory.getLogger(DemoDataConfig.class);

    public static final UUID BIBLIOTECARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID ANA_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID LUIS_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final UUID DOCENTE_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    public static final UUID LIBRO_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    public static final UUID LAPTOP_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");

    public static final UUID EJEMPLAR_LIBRO_1 = UUID.fromString("00000000-0000-0000-0000-000000000201");
    public static final UUID EJEMPLAR_LIBRO_2 = UUID.fromString("00000000-0000-0000-0000-000000000202");
    public static final UUID EJEMPLAR_LAPTOP_1 = UUID.fromString("00000000-0000-0000-0000-000000000203");

    public static final UUID PRESTAMO_VENCIDO_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");

    @Bean
    public ApplicationRunner cargarDatosDemo(UsuarioRepository usuarios,
                                             CatalogoItemRepository catalogo,
                                             EjemplarRepository ejemplares,
                                             PrestamoRepository prestamos) {
        return args -> {
            if (!usuarios.listarTodos().isEmpty()) {
                log.info("Datos demo no cargados: la base ya contiene usuarios.");
                return;
            }

            Instant ahora = Instant.now();
            LocalDate hoy = LocalDate.now();

            Usuario bibliotecario = Usuario.reconstruir(
                    new UsuarioId(BIBLIOTECARIO_ID),
                    new EmailAddress("bibliotecario@biblioteca.pe"),
                    new NombreCompleto("Elena Bibliotecaria"),
                    PerfilPrestatario.ADMINISTRATIVO,
                    EstadoCuentaUsuario.VALIDADO,
                    EnumSet.allOf(PermisoOperativo.class),
                    ahora.minus(365, ChronoUnit.DAYS),
                    ahora.minus(300, ChronoUnit.DAYS),
                    null);

            Usuario ana = Usuario.reconstruir(
                    new UsuarioId(ANA_ID),
                    new EmailAddress("ana@universidad.edu.pe"),
                    new NombreCompleto("Ana Torres"),
                    PerfilPrestatario.ALUMNO,
                    EstadoCuentaUsuario.PROVISIONAL,
                    EnumSet.noneOf(PermisoOperativo.class),
                    ahora.minus(2, ChronoUnit.DAYS),
                    null,
                    null);

            Usuario luis = Usuario.reconstruir(
                    new UsuarioId(LUIS_ID),
                    new EmailAddress("luis@universidad.edu.pe"),
                    new NombreCompleto("Luis Quispe"),
                    PerfilPrestatario.ALUMNO,
                    EstadoCuentaUsuario.VALIDADO,
                    EnumSet.noneOf(PermisoOperativo.class),
                    ahora.minus(200, ChronoUnit.DAYS),
                    ahora.minus(190, ChronoUnit.DAYS),
                    null);

            Usuario docente = Usuario.reconstruir(
                    new UsuarioId(DOCENTE_ID),
                    new EmailAddress("docente@universidad.edu.pe"),
                    new NombreCompleto("Rosa Docente"),
                    PerfilPrestatario.DOCENTE,
                    EstadoCuentaUsuario.VALIDADO,
                    EnumSet.noneOf(PermisoOperativo.class),
                    ahora.minus(400, ChronoUnit.DAYS),
                    ahora.minus(390, ChronoUnit.DAYS),
                    null);

            usuarios.guardar(bibliotecario);
            usuarios.guardar(ana);
            usuarios.guardar(luis);
            usuarios.guardar(docente);

            CatalogoItem libro = CatalogoItem.reconstruir(
                    new CatalogoItemId(LIBRO_ID),
                    new Titulo("Clean Code"),
                    TipoItem.LIBRO,
                    new LibroDetalle(new ISBN("9780132350884"), new Autor("Robert C. Martin"),
                            "Prentice Hall", 2008),
                    true,
                    ahora.minus(30, ChronoUnit.DAYS));

            CatalogoItem laptop = CatalogoItem.reconstruir(
                    new CatalogoItemId(LAPTOP_ID),
                    new Titulo("Laptop Lenovo ThinkPad T14"),
                    TipoItem.EQUIPO,
                    new EquipoDetalle("Lenovo", "ThinkPad T14"),
                    true,
                    ahora.minus(20, ChronoUnit.DAYS));

            catalogo.guardar(libro);
            catalogo.guardar(laptop);

            Ejemplar libro1 = Ejemplar.reconstruir(
                    new EjemplarId(EJEMPLAR_LIBRO_1),
                    libro.id(),
                    new Barcode("LIB-001"),
                    new Ubicacion("Sala general", "Estante A-1"),
                    EstadoFisico.DISPONIBLE,
                    "Primera copia de Clean Code",
                    ahora.minus(30, ChronoUnit.DAYS));

            Ejemplar libro2 = Ejemplar.reconstruir(
                    new EjemplarId(EJEMPLAR_LIBRO_2),
                    libro.id(),
                    new Barcode("LIB-002"),
                    new Ubicacion("Sala general", "Estante A-1"),
                    EstadoFisico.PRESTADO,
                    "Copia prestada a Luis (prestamo vencido para demo)",
                    ahora.minus(30, ChronoUnit.DAYS));

            Ejemplar laptop1 = Ejemplar.reconstruir(
                    new EjemplarId(EJEMPLAR_LAPTOP_1),
                    laptop.id(),
                    new Barcode("LAP-001"),
                    new Ubicacion("Laboratorio", "Gabinete 2"),
                    EstadoFisico.DISPONIBLE,
                    "Laptop de prestamo en campus",
                    ahora.minus(20, ChronoUnit.DAYS));

            ejemplares.guardar(libro1);
            ejemplares.guardar(libro2);
            ejemplares.guardar(laptop1);

            Prestamo vencido = Prestamo.reconstruir(
                    new PrestamoId(PRESTAMO_VENCIDO_ID),
                    bibliotecario.id(),
                    luis.id(),
                    libro2.id(),
                    new PeriodoPrestamo(hoy.minusDays(10), hoy.minusDays(3)),
                    LugarUso.EN_CAMPUS,
                    EstadoPrestamo.ACTIVO,
                    null,
                    0,
                    "Prestamo vencido para demostrar la sancion automatica",
                    ahora.minus(10, ChronoUnit.DAYS));

            prestamos.guardar(vencido);

            log.info("""

                    ============================================================
                     DATOS DEMO CARGADOS (perfil demo, persistencia JPA/PostgreSQL)
                    ============================================================
                     Bibliotecario : {}
                     Ana (PROVISIONAL, no puede prestar): {}
                     Luis (ALUMNO validado)            : {}
                     Docente (DOCENTE validado)        : {}

                     Libro Clean Code                  : {}
                     Laptop Lenovo                     : {}
                     Ejemplar disponible LIB-001       : {}
                     Ejemplar prestado   LIB-002       : {}
                     Ejemplar laptop     LAP-001       : {}
                     Prestamo VENCIDO de Luis          : {}

                     Demo sugerida:
                       POST /api/v1/prestamos/<id>/devolucion
                       GET  /api/v1/sanciones
                    ============================================================
                    """,
                    bibliotecario.id().value(), ana.id().value(), luis.id().value(),
                    docente.id().value(), libro.id().value(), laptop.id().value(),
                    libro1.id().value(), libro2.id().value(), laptop1.id().value(),
                    vencido.id().value());
        };
    }
}
