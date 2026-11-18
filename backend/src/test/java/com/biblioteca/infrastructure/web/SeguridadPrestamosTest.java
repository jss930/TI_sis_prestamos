package com.biblioteca.infrastructure.web;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.model.*;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.infrastructure.security.TokenService;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.support.Fixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SeguridadPrestamosTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EjemplarRepository ejemplares;
    @Autowired PrestamoRepository prestamos;
    @Autowired TokenService tokens;
    Usuario alumno, otro, admin;
    Prestamo propio, ajeno;
    com.biblioteca.catalog.domain.model.CatalogoItem libro;

    @BeforeEach
    void preparar() {
        alumno = usuarios.guardar(Fixtures.alumnoValidado());
        otro = usuarios.guardar(Fixtures.alumnoValidado());
        admin = usuarios.guardar(Fixtures.bibliotecario());
        libro = catalogo.guardar(Fixtures.libro());
        propio = registrar(alumno);
        ajeno = registrar(otro);
    }

    Prestamo registrar(Usuario usuario) {
        var copia = ejemplares.guardar(Fixtures.ejemplarDisponible(libro.id()));
        return prestamos.guardar(Prestamo.registrar(admin.id(), usuario.id(), copia.id(),
                new PeriodoPrestamo(Fixtures.HOY, Fixtures.HOY), LugarUso.EN_CAMPUS,
                Fixtures.AHORA, Fixtures.AHORA.plusSeconds(3600)));
    }

    String token(Usuario u) { return "Bearer " + tokens.generar(u.id().value(), u.email().value()); }
    String ruta(Prestamo p) { return "/api/v1/prestamos/" + p.id().value(); }

    @Test
    void listadoDetalleYRenovacionExigenToken() throws Exception {
        mvc.perform(get("/api/v1/prestamos")).andExpect(status().isUnauthorized());
        mvc.perform(get(ruta(propio))).andExpect(status().isUnauthorized());
        mvc.perform(post(ruta(propio) + "/renovacion")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/prestamos").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/reservas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/sanciones")).andExpect(status().isUnauthorized());
    }

    @Test
    void propietarioSoloRecibeSusPrestamos() throws Exception {
        mvc.perform(get("/api/v1/prestamos").header("Authorization", token(alumno)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(propio.id().value().toString()));
        mvc.perform(get(ruta(propio)).header("Authorization", token(alumno))).andExpect(status().isOk());
        mvc.perform(get(ruta(ajeno)).header("Authorization", token(alumno))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/prestamos").param("prestatarioId", otro.id().value().toString())
                .header("Authorization", token(alumno))).andExpect(status().isForbidden());
        mvc.perform(post(ruta(ajeno) + "/renovacion").header("Authorization", token(alumno)))
                .andExpect(status().isForbidden());
    }

    @Test
    void personalAutorizadoPuedeConsultarYRevocarPermisoTieneEfectoInmediato() throws Exception {
        mvc.perform(get(ruta(ajeno)).header("Authorization", token(admin))).andExpect(status().isOk());
        var operador = usuarios.guardar(Fixtures.alumnoValidado());
        operador.otorgarPermiso(com.biblioteca.identity.domain.model.PermisoOperativo.REGISTRAR_PRESTAMO);
        usuarios.guardar(operador);
        String token = token(operador);
        mvc.perform(get(ruta(ajeno)).header("Authorization", token)).andExpect(status().isOk());
        operador.revocarPermiso(com.biblioteca.identity.domain.model.PermisoOperativo.REGISTRAR_PRESTAMO);
        usuarios.guardar(operador);
        mvc.perform(get(ruta(ajeno)).header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    void noSePuedeConsultarSancionesDeOtroUsuario() throws Exception {
        mvc.perform(get("/api/v1/sanciones").param("usuarioId", otro.id().value().toString())
                .header("Authorization", token(alumno))).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/sanciones/" + java.util.UUID.randomUUID() + "/cumplir")
                .header("Authorization", token(alumno))).andExpect(status().isForbidden());
    }
}
