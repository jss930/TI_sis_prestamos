package com.biblioteca.infrastructure.web;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.model.*;
import com.biblioteca.circulation.domain.repository.*;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.identity.domain.model.*;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.infrastructure.security.TokenService;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.support.Fixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HistorialesTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EjemplarRepository ejemplares;
    @Autowired PrestamoRepository prestamos;
    @Autowired ReservaRepository reservas;
    @Autowired InspeccionDevolucionRepository inspecciones;
    @Autowired TokenService tokens;
    Usuario alumno, otro, admin;
    Ejemplar copia, otraCopia;
    Prestamo propio;

    @BeforeEach void preparar() {
        alumno = usuarios.guardar(Fixtures.alumnoValidado());
        otro = usuarios.guardar(Fixtures.alumnoValidado());
        admin = usuarios.guardar(Fixtures.bibliotecario());
        var libro = catalogo.guardar(Fixtures.libro());
        copia = ejemplares.guardar(Fixtures.ejemplarDisponible(libro.id()));
        otraCopia = ejemplares.guardar(Fixtures.ejemplarDisponible(libro.id()));
        propio = prestamos.guardar(Prestamo.registrar(admin.id(), alumno.id(), copia.id(),
                new PeriodoPrestamo(Fixtures.HOY, Fixtures.HOY), LugarUso.EN_CAMPUS,
                Fixtures.AHORA, Fixtures.AHORA.plusSeconds(3600)));
        prestamos.guardar(Prestamo.registrar(admin.id(), otro.id(), otraCopia.id(),
                new PeriodoPrestamo(Fixtures.HOY, Fixtures.HOY), LugarUso.EN_CAMPUS,
                Fixtures.AHORA, Fixtures.AHORA.plusSeconds(3600)));
    }

    String token(Usuario u) { return "Bearer " + tokens.generar(u.id().value(), u.email().value()); }
    String usuario(Usuario u) { return "/api/v1/reportes/usuarios/" + u.id().value(); }
    String ejemplar(Ejemplar e) { return "/api/v1/reportes/ejemplares/" + e.id().value(); }

    @Test void autenticacionYPrivacidadPorUsuario() throws Exception {
        mvc.perform(get(usuario(alumno))).andExpect(status().isUnauthorized());
        mvc.perform(get(ejemplar(copia))).andExpect(status().isUnauthorized());
        mvc.perform(get(usuario(otro)).header("Authorization", token(alumno))).andExpect(status().isForbidden());
        mvc.perform(get(ejemplar(copia)).header("Authorization", token(alumno))).andExpect(status().isForbidden());
        mvc.perform(get(usuario(alumno)).header("Authorization", token(alumno)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.usuario.id").value(alumno.id().value().toString()))
                .andExpect(jsonPath("$.prestamos", hasSize(1)))
                .andExpect(jsonPath("$.prestamos[0].prestatarioId").value(alumno.id().value().toString()))
                .andExpect(jsonPath("$.resumen.totalPrestamos").value(1))
                .andExpect(jsonPath("$.usuario.permisos").doesNotExist())
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test void personalDeVentanillaNoObtieneReportesAdministrativos() throws Exception {
        alumno.otorgarPermiso(PermisoOperativo.REGISTRAR_PRESTAMO); usuarios.guardar(alumno);
        mvc.perform(get(usuario(otro)).header("Authorization", token(alumno))).andExpect(status().isForbidden());
        mvc.perform(get(ejemplar(copia)).header("Authorization", token(alumno))).andExpect(status().isForbidden());
    }

    @Test void administradorVeSoloLaCopiaConsultadaYSuInspeccion() throws Exception {
        propio.registrarDevolucion(Fixtures.HOY, Fixtures.AHORA.plusSeconds(7200), EstadoFisico.DISPONIBLE);
        prestamos.guardar(propio);
        inspecciones.guardar(new InspeccionDevolucion(propio.id().value(), admin.id().value(),
                EstadoFisico.DISPONIBLE, true, "Falta el accesorio", Fixtures.AHORA.plusSeconds(7200)));
        mvc.perform(get(ejemplar(copia)).header("Authorization", token(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ejemplar.barcode").value(copia.barcode().value()))
                .andExpect(jsonPath("$.prestamos", hasSize(1)))
                .andExpect(jsonPath("$.prestamos[0].ejemplarId").value(copia.id().value().toString()))
                .andExpect(jsonPath("$.prestamos[0].inspeccion.recibidoPor").value(admin.nombreCompleto().value()))
                .andExpect(jsonPath("$.prestamos[0].inspeccion.accesoriosFaltantes").value(true))
                .andExpect(jsonPath("$.prestamos[0].devolucionTardia").value(true))
                .andExpect(jsonPath("$.resumen.devolucionesTardias").value(1));
    }

    @Test void filtrosInclusivosDeFechaYEstado() throws Exception {
        mvc.perform(get(usuario(alumno)).header("Authorization", token(admin))
                .param("desde", "2026-10-20").param("hasta", "2026-10-20").param("estado", "ACTIVO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.prestamos", hasSize(1)));
        mvc.perform(get(usuario(alumno)).header("Authorization", token(admin))
                .param("desde", "2026-10-21"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.prestamos", hasSize(0)))
                .andExpect(jsonPath("$.resumen.totalPrestamos").value(0));
        mvc.perform(get(usuario(alumno)).header("Authorization", token(admin)).param("estado", "DEVUELTO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.prestamos", hasSize(0)));
        mvc.perform(get(usuario(alumno)).header("Authorization", token(admin))
                .param("desde", "2026-10-21").param("hasta", "2026-10-20"))
                .andExpect(status().isBadRequest());
    }

    @Test void recursosInexistentesYFechasInvalidas() throws Exception {
        mvc.perform(get("/api/v1/reportes/usuarios/" + UUID.randomUUID()).header("Authorization", token(admin)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/reportes/ejemplares/" + UUID.randomUUID()).header("Authorization", token(admin)))
                .andExpect(status().isNotFound());
        mvc.perform(get(usuario(alumno)).param("desde", "no-es-fecha").header("Authorization", token(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test void reservaSinAsignacionNoSeAtribuyeAUnaCopia() throws Exception {
        var inicio = Fixtures.AHORA.plus(Duration.ofDays(1));
        var asignada = Reserva.solicitarProgramada(alumno.id(), copia.catalogoItemId(), LugarUso.EN_CAMPUS,
                inicio, inicio.plus(Duration.ofHours(2)), Fixtures.AHORA, ZoneId.of("America/Lima"));
        asignada.aprobarProgramada(admin.id(), copia.id(), Fixtures.AHORA);
        reservas.guardar(asignada);
        reservas.guardar(Reserva.solicitarProgramada(otro.id(), copia.catalogoItemId(), LugarUso.EN_CAMPUS,
                inicio, inicio.plus(Duration.ofHours(2)), Fixtures.AHORA, ZoneId.of("America/Lima")));
        mvc.perform(get(ejemplar(copia)).header("Authorization", token(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reservas", hasSize(1)))
                .andExpect(jsonPath("$.reservas[0].id").value(asignada.id().value().toString()));
        mvc.perform(get(ejemplar(otraCopia)).header("Authorization", token(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reservas", hasSize(0)));
    }
}
