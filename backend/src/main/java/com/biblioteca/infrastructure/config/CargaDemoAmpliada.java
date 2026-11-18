package com.biblioteca.infrastructure.config;

import com.biblioteca.catalog.domain.model.*;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.*;
import com.biblioteca.circulation.domain.model.*;
import com.biblioteca.circulation.domain.policy.*;
import com.biblioteca.circulation.domain.repository.*;
import com.biblioteca.circulation.domain.vo.*;
import com.biblioteca.identity.domain.model.*;
import com.biblioteca.identity.domain.repository.*;
import com.biblioteca.identity.domain.vo.*;
import com.biblioteca.infrastructure.security.PasswordHasher;
import com.biblioteca.inventory.domain.model.*;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.inventory.domain.vo.*;
import com.biblioteca.penalties.domain.model.*;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.penalties.domain.vo.*;
import com.biblioteca.reservations.domain.model.*;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

/** Invocada solo por el runner del perfil demo. Nunca modifica filas preexistentes. */
@Service
public class CargaDemoAmpliada {
    private static final Logger log = LoggerFactory.getLogger(CargaDemoAmpliada.class);
    private static final String VERSION = "ampliada-v1";
    private final JdbcTemplate jdbc;
    private final UsuarioRepository usuarios;
    private final CredencialRepository credenciales;
    private final CatalogoItemRepository catalogo;
    private final EjemplarRepository ejemplares;
    private final PrestamoRepository prestamos;
    private final ReservaRepository reservas;
    private final SancionRepository sanciones;
    private final InspeccionDevolucionRepository inspecciones;
    private final PasswordHasher passwords;
    private final Clock clock;
    private final PoliticaPrestamo politica = new PoliticaPrestamoEstandar();

    public CargaDemoAmpliada(JdbcTemplate jdbc, UsuarioRepository usuarios, CredencialRepository credenciales,
            CatalogoItemRepository catalogo, EjemplarRepository ejemplares, PrestamoRepository prestamos,
            ReservaRepository reservas, SancionRepository sanciones, InspeccionDevolucionRepository inspecciones,
            PasswordHasher passwords, Clock clock) {
        this.jdbc = jdbc; this.usuarios = usuarios; this.credenciales = credenciales; this.catalogo = catalogo;
        this.ejemplares = ejemplares; this.prestamos = prestamos; this.reservas = reservas;
        this.sanciones = sanciones; this.inspecciones = inspecciones; this.passwords = passwords; this.clock = clock;
    }

    public static UUID id(String tipo, int numero) {
        return UUID.nameUUIDFromBytes(("demo-ampliada-v1:" + tipo + ":" + numero).getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public boolean cargar() {
        // La fila se bloquea incluso antes de crear registros: serializa dos arranques.
        if (Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT completada FROM cargas_demo WHERE clave = ? FOR UPDATE", Boolean.class, VERSION))) return false;
        String[] correos = { "operador.demo", "maria.demo", "jose.demo", "lucia.demo",
                "carlos.demo", "miguel.demo", "provisional.demo", "suspendida.demo" };
        for (int i = 0; i < correos.length; i++) {
            if (usuarios.buscarPorId(new UsuarioId(id("usuario", i))).isPresent()
                    || usuarios.buscarPorEmail(new EmailAddress(correos[i] + "@universidad.edu.pe")).isPresent()) {
                log.warn("Carga demo ampliada omitida: existe una cuenta con identificador reservado. No se sobrescribio.");
                return false;
            }
        }
        for (int i = 0; i < 18; i++) {
            if (ejemplares.buscarPorId(new EjemplarId(id("ejemplar", i))).isPresent()
                    || ejemplares.existePorBarcode(new Barcode(barcode(i)))) {
                log.warn("Carga demo ampliada omitida: existe un ejemplar con identificador reservado.");
                return false;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (catalogo.buscarPorId(new CatalogoItemId(id("catalogo", i))).isPresent()) {
                log.warn("Carga demo ampliada omitida: existe un recurso con identificador reservado.");
                return false;
            }
        }
        for (int i = 0; i < 40; i++) {
            if (prestamos.buscarPorId(new PrestamoId(id("prestamo", i))).isPresent()
                    || sanciones.buscarPorId(new SancionId(id("sancion", i))).isPresent()) return false;
        }
        if (sanciones.buscarPorId(new SancionId(id("sancion", 99))).isPresent()) return false;
        for (int i = 0; i < 6; i++) if (reservas.buscarPorId(new ReservaId(id("reserva", i))).isPresent()) return false;
        var ahora = clock.instant();
        var hoy = LocalDate.ofInstant(ahora, PoliticaPlazoPrestamo.ZONA);
        String[] nombres = { "Diego Operador Demo", "María Salas Demo", "José Mendoza Demo", "Lucía Docente Demo",
                "Carlos Administrativo Demo", "Miguel Docente Demo", "Rosa Provisional Demo", "Valeria Suspendida Demo" };
        PerfilPrestatario[] perfiles = { PerfilPrestatario.ADMINISTRATIVO, PerfilPrestatario.ALUMNO, PerfilPrestatario.ALUMNO,
                PerfilPrestatario.DOCENTE, PerfilPrestatario.ADMINISTRATIVO, PerfilPrestatario.DOCENTE,
                PerfilPrestatario.ALUMNO, PerfilPrestatario.ALUMNO };
        var cuentas = new ArrayList<Usuario>();
        String hash = passwords.hash("password");
        for (int i = 0; i < correos.length; i++) {
            var estado = i == 6 ? EstadoCuentaUsuario.PROVISIONAL : i == 7 ? EstadoCuentaUsuario.SUSPENDIDO : EstadoCuentaUsuario.VALIDADO;
            var cuenta = Usuario.reconstruirConRevision(new UsuarioId(id("usuario", i)),
                    new EmailAddress(correos[i] + "@universidad.edu.pe"), new NombreCompleto(nombres[i]), perfiles[i], estado,
                    i == 0 ? EnumSet.allOf(PermisoOperativo.class) : EnumSet.noneOf(PermisoOperativo.class),
                    ahora.minus(Duration.ofDays(120)), i == 6 ? null : ahora.minus(Duration.ofDays(100)),
                    i == 7 ? "Suspension de demostracion" : null, new CodigoInstitucional("DEMO-" + (1000 + i)),
                    i == 6 ? null : new UsuarioId(id("usuario", 0)), null, null, null, false);
            usuarios.guardar(cuenta);
            credenciales.guardar(cuenta.id(), hash);
            cuentas.add(cuenta);
        }
        var operador = cuentas.getFirst();
        var recursos = recursos(ahora);
        var copias = new ArrayList<Ejemplar>();
        for (int i = 0; i < 18; i++) {
            var copia = Ejemplar.reconstruir(new EjemplarId(id("ejemplar", i)), recursos.get(i / 2).id(),
                    new Barcode(barcode(i)), new Ubicacion("Sala demo", "Modulo " + (i / 6 + 1)),
                    EstadoFisico.DISPONIBLE, "Ejemplar de demostracion ampliada", ahora.minus(Duration.ofDays(90)));
            copias.add(ejemplares.guardar(copia));
        }
        var elena = usuarios.buscarPorId(new UsuarioId(DemoDataConfig.BIBLIOTECARIO_ID))
                .filter(u -> u.email().value().equals("bibliotecario@biblioteca.pe") && u.puedeSolicitarPrestamo())
                .orElse(cuentas.get(4));
        var prestatarios = List.of(cuentas.get(1), cuentas.get(2), cuentas.get(3), cuentas.get(4), cuentas.get(5), elena);
        var historicos = new ArrayList<Prestamo>();
        for (int i = 0; i < 36; i++) {
            var persona = prestatarios.get(i % prestatarios.size());
            var copia = copias.get(i % copias.size());
            var recurso = recursos.get((i % copias.size()) / 2);
            var inicio = instante(hoy.minusDays(50 - i), 10);
            var lugar = politica.permiteDomicilio(persona.perfil(), recurso.tipo()) ? LugarUso.DOMICILIO : LugarUso.EN_CAMPUS;
            var fin = lugar == LugarUso.DOMICILIO ? inicio.plus(Duration.ofDays(politica.diasPrestamo(persona.perfil(), recurso.tipo())))
                    : inicio.plus(Duration.ofHours(2));
            var p = prestamo(i, operador, persona, copia, recurso, lugar, inicio, fin, i % 8 == 0 ? 1 : 0);
            boolean tarde = i % 7 == 0;
            var retorno = tarde ? fin.plus(Duration.ofDays(1)) : fin.minus(Duration.ofMinutes(30));
            var fisico = i == 33 ? EstadoFisico.PERDIDO : i == 34 ? EstadoFisico.DANADO : EstadoFisico.DISPONIBLE;
            boolean faltantes = i == 35;
            String observacion = i == 33 ? "Extravio comunicado por el prestatario (demo)"
                    : i == 34 ? "Dano detectado en la inspeccion (demo)" : faltantes ? "Falta una pieza del kit (demo)"
                    : tarde ? "Devolucion fuera de plazo (demo)" : "Devolucion conforme (demo)";
            p.registrarDevolucion(LocalDate.ofInstant(retorno, PoliticaPlazoPrestamo.ZONA), retorno, fisico);
            prestamos.guardar(p);
            var inspeccion = new InspeccionDevolucion(p.id().value(), operador.id().value(), fisico, faltantes, observacion, retorno);
            inspecciones.guardar(inspeccion);
            copia.prestar(); copia.devolver(inspeccion.estadoResultante()); ejemplares.guardar(copia);
            if (tarde || i >= 33) {
                var fecha = LocalDate.ofInstant(retorno, PoliticaPlazoPrestamo.ZONA);
                sanciones.guardar(Sancion.reconstruir(new SancionId(id("sancion", i)), persona.id(), p.id(),
                        i == 33 ? MotivoSancion.PERDIDA : i >= 34 ? MotivoSancion.DANO : MotivoSancion.RETRASO,
                        2, MontoMulta.deSoles(java.math.BigDecimal.ZERO), fecha, fecha.plusDays(2),
                        EstadoSancion.CUMPLIDA, observacion));
            }
            historicos.add(p);
        }
        var ayer = hoy.minusDays(1);
        activo(36, operador, cuentas.get(3), copias.get(0), recursos.get(0), LugarUso.DOMICILIO, instante(ayer, 10), instante(ayer.plusDays(5), 10));
        activo(37, operador, cuentas.get(4), copias.get(6), recursos.get(3), LugarUso.DOMICILIO, instante(ayer, 10), instante(ayer.plusDays(3), 10));
        activo(38, operador, cuentas.get(1), copias.get(12), recursos.get(6), LugarUso.EN_CAMPUS, instante(ayer, 10), instante(ayer, 12));
        // No agregamos un activo a Elena si sus operaciones reales ya la bloquean.
        var receptor = elena.puedeSolicitarPrestamo() && !prestamos.tieneVencido(elena.id(), ahora)
                && !sanciones.existeActivaPara(elena.id(), hoy)
                && prestamos.contarActivosPor(elena.id()) + reservas.contarActivasPor(elena.id()) < 3 ? elena : cuentas.get(2);
        activo(39, operador, receptor, copias.get(1), recursos.get(0), LugarUso.DOMICILIO, instante(ayer, 10), instante(ayer.plusDays(3), 10));
        sanciones.guardar(Sancion.manual(new SancionId(id("sancion", 99)), cuentas.get(7).id(), null,
                MotivoSancion.DANO, 3, MontoMulta.deSoles(java.math.BigDecimal.valueOf(20)), hoy, "Sancion activa de demostracion"));

        reserva(0, cuentas.get(2), operador, copias.get(13), EstadoReserva.SOLICITADA, instante(hoy.plusDays(2), 10), null);
        reserva(1, cuentas.get(5), operador, copias.get(8), EstadoReserva.APROBADA, instante(hoy.plusDays(2), 10), null);
        reserva(2, cuentas.get(2), operador, copias.get(14), EstadoReserva.CANCELADA, instante(hoy.minusDays(10), 10), null);
        reserva(3, cuentas.get(5), operador, copias.get(10), EstadoReserva.DENEGADA, instante(hoy.minusDays(9), 10), null);
        reserva(4, cuentas.get(3), operador, copias.get(10), EstadoReserva.EXPIRADA, instante(hoy.minusDays(8), 10), null);
        var atendida = historicos.get(30);
        reserva(5, cuentas.get(1), operador, copias.get(12), EstadoReserva.ATENDIDA, atendida.fechaHoraEntrega(), atendida);
        jdbc.update("UPDATE cargas_demo SET completada = TRUE, fecha = ? WHERE clave = ?",
                java.sql.Timestamp.from(ahora), VERSION);
        log.info("Demo ampliada cargada una vez: 8 cuentas, 9 recursos, 18 copias, 40 prestamos y 6 reservas. Clave demo: password.");
        return true;
    }

    private List<CatalogoItem> recursos(Instant ahora) {
        String[] titulos = { "Introduction to Algorithms", "Domain-Driven Design", "Database System Concepts",
                "Laptop Dell Latitude 5440 (demo)", "Proyector Epson EB-X49 (demo)", "Camara Canon EOS M50 (demo)",
                "Kit de ajedrez (demo)", "Balon de voley (demo)", "Kit de laboratorio (demo)" };
        String[] isbn = { "9780262033848", "9780321125217", "9780073523323" };
        var lista = new ArrayList<CatalogoItem>();
        for (int i = 0; i < titulos.length; i++) {
            var tipo = i < 3 ? TipoItem.LIBRO : i < 6 ? TipoItem.EQUIPO : TipoItem.OTRO;
            // Si ya existe el ISBN, reutiliza metadatos sin actualizarlos.
            final int indice = i;
            var existente = i < 3 ? catalogo.listarTodos().stream().filter(c -> c.detalle() instanceof LibroDetalle libro
                    && libro.isbn().value().equals(isbn[indice])).findFirst() : Optional.<CatalogoItem>empty();
            if (existente.isPresent()) { lista.add(existente.get()); continue; }
            DetalleItem detalle = i < 3 ? new LibroDetalle(new ISBN(isbn[i]), new Autor(i == 0 ? "Thomas H. Cormen" : i == 1 ? "Eric Evans" : "Abraham Silberschatz"), "Edicion academica", 2010)
                    : i < 6 ? new EquipoDetalle(i == 3 ? "Dell" : i == 4 ? "Epson" : "Canon", titulos[i])
                    : new OtroDetalle("Material de demostracion para uso exclusivo en campus");
            lista.add(catalogo.guardar(CatalogoItem.reconstruir(new CatalogoItemId(id("catalogo", i)),
                    new Titulo(titulos[i]), tipo, detalle, true, ahora.minus(Duration.ofDays(90)))));
        }
        return lista;
    }

    private Prestamo prestamo(int indice, Usuario operador, Usuario persona, Ejemplar copia, CatalogoItem recurso,
                              LugarUso lugar, Instant inicio, Instant fin, int renovaciones) {
        PoliticaPlazoPrestamo.validar(inicio, fin, lugar, persona.perfil(), recurso.tipo(), politica);
        return Prestamo.reconstruir(new PrestamoId(id("prestamo", indice)), operador.id(), persona.id(), copia.id(),
                new PeriodoPrestamo(LocalDate.ofInstant(inicio, PoliticaPlazoPrestamo.ZONA), LocalDate.ofInstant(fin, PoliticaPlazoPrestamo.ZONA)),
                lugar, EstadoPrestamo.ACTIVO, null, renovaciones, "Operacion de demostracion ampliada",
                inicio, inicio, fin, null);
    }

    private void activo(int indice, Usuario operador, Usuario persona, Ejemplar copia, CatalogoItem recurso,
                         LugarUso lugar, Instant inicio, Instant fin) {
        prestamos.guardar(prestamo(indice, operador, persona, copia, recurso, lugar, inicio, fin, 0));
        copia.prestar(); ejemplares.guardar(copia);
    }

    private void reserva(int indice, Usuario persona, Usuario operador, Ejemplar copia, EstadoReserva estado, Instant inicio, Prestamo prestamo) {
        var alta = inicio.minus(Duration.ofDays(1));
        var ultimaAlta = clock.instant().minus(Duration.ofHours(2));
        if (alta.isAfter(ultimaAlta)) alta = ultimaAlta;
        boolean asignada = estado == EstadoReserva.APROBADA || estado == EstadoReserva.EXPIRADA || estado == EstadoReserva.ATENDIDA;
        reservas.guardar(Reserva.reconstruirSolicitud(new ReservaId(id("reserva", indice)), persona.id(), copia.catalogoItemId(),
                estado, estado == EstadoReserva.SOLICITADA || estado == EstadoReserva.CANCELADA ? null : operador.id(),
                prestamo == null ? null : prestamo.id(), estado == EstadoReserva.DENEGADA ? "Solicitud denegada para demostracion" : null,
                alta, estado == EstadoReserva.SOLICITADA ? null : alta.plus(Duration.ofHours(1)), asignada ? copia.id() : null,
                asignada ? inicio.plus(Duration.ofHours(1)) : null, prestamo == null ? null : inicio,
                prestamo == null ? null : operador.id(), LugarUso.EN_CAMPUS, inicio, inicio.plus(Duration.ofHours(2))));
    }

    private static Instant instante(LocalDate fecha, int hora) {
        return fecha.atTime(hora, 0).atZone(PoliticaPlazoPrestamo.ZONA).toInstant();
    }

    private static String barcode(int indice) { return "DEMO-" + (indice < 6 ? "LIB" : indice < 12 ? "EQU" : "MAT") + "-" + String.format("%02d", indice + 1); }
}
