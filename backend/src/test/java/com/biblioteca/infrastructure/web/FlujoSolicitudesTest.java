package com.biblioteca.infrastructure.web;

import com.biblioteca.catalog.domain.model.AreaCatalogo;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.identity.domain.model.*;
import com.biblioteca.identity.domain.repository.*;
import com.biblioteca.infrastructure.security.TokenService;
import com.biblioteca.inventory.domain.model.*;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.domain.model.*;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.application.usecase.ExpirarSolicitudUseCase;
import com.biblioteca.support.Fixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FlujoSolicitudesTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired ResponsabilidadRepository responsabilidades;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EjemplarRepository ejemplares;
    @Autowired ReservaRepository reservas;
    @Autowired PrestamoRepository prestamos;
    @Autowired TokenService tokens;
    @Autowired EntityManager em;
    @Autowired Clock clock;
    @Autowired ExpirarSolicitudUseCase expirar;
    Usuario alumno, responsable;
    Ejemplar ejemplar;
    Reserva solicitud;
    String token;

    @BeforeEach void preparar() {
        alumno=usuarios.guardar(Fixtures.alumnoValidado());
        responsable=Fixtures.alumnoValidado();
        responsable.otorgarPermiso(PermisoOperativo.REGISTRAR_PRESTAMO);
        responsable.otorgarPermiso(PermisoOperativo.GESTIONAR_RESERVAS);
        usuarios.guardar(responsable);
        responsabilidades.guardar(new ResponsabilidadUsuario(responsable.id(), false, Set.of(CategoriaResponsable.BIBLIOTECA)));
        var libro=catalogo.guardar(Fixtures.libro());
        ejemplar=ejemplares.guardar(Fixtures.ejemplarDisponible(libro.id()));
        solicitud=reservas.guardar(Reserva.solicitar(alumno.id(), libro.id(), LugarUso.EN_CAMPUS, clock.instant()));
        token=token(responsable);
        em.flush(); em.clear();
    }
    String token(Usuario u) { return "Bearer " + tokens.generar(u.id().value(), u.email().value()); }
    String ruta(String accion) { return "/api/v1/reservas/" + solicitud.id().value() + "/" + accion; }
    void aprobar() throws Exception {
        mvc.perform(post(ruta("aceptar")).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"operadorId\":\""+responsable.id().value()+"\",\"ejemplarId\":\""+ejemplar.id().value()+"\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("APROBADA"))
            .andExpect(jsonPath("$.prestamoId").isEmpty());
        em.flush(); em.clear();
    }
    @Test void responsableApruebaSinCrearPrestamoYEntregaDespues() throws Exception {
        aprobar();
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estadoFisico()).isEqualTo(EstadoFisico.RESERVADO);
        assertThat(prestamos.existeActivoPara(ejemplar.id())).isFalse();
        var aprobada=reservas.buscarPorId(solicitud.id()).orElseThrow();
        assertThat(Duration.between(aprobada.fechaDecision(), aprobada.fechaLimiteRetiro()).toHours()).isEqualTo(48);
        mvc.perform(post(ruta("entregar")).header("Authorization", token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ATENDIDA"))
            .andExpect(jsonPath("$.entregadoPorId").value(responsable.id().value().toString()));
        em.flush(); em.clear();
        var atendida=reservas.buscarPorId(solicitud.id()).orElseThrow();
        var prestamo=prestamos.buscarPorId(atendida.prestamoId()).orElseThrow();
        assertThat(prestamo.ejemplarId()).isEqualTo(ejemplar.id());
        assertThat(prestamo.periodo().inicio()).isEqualTo(LocalDate.now(clock));
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estadoFisico()).isEqualTo(EstadoFisico.PRESTADO);
        mvc.perform(post(ruta("entregar")).header("Authorization", token)).andExpect(status().isConflict());
    }
    @Test void otroResponsableDelAreaPuedeEntregarConAuditoriaSeparada() throws Exception {
        aprobar();
        var otro=Fixtures.alumnoValidado(); otro.otorgarPermiso(PermisoOperativo.REGISTRAR_PRESTAMO); usuarios.guardar(otro);
        responsabilidades.guardar(ResponsabilidadUsuario.reconstruir(otro.id(), false, java.util.Map.of(CategoriaResponsable.BIBLIOTECA, CargoResponsable.SUPLENTE)));
        mvc.perform(post(ruta("entregar")).header("Authorization", token(otro)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.operadorId").value(responsable.id().value().toString()))
            .andExpect(jsonPath("$.entregadoPorId").value(otro.id().value().toString()));
    }
    @Test void responsableDeOtraAreaNoVeNiDecideLaSolicitud() throws Exception {
        responsabilidades.guardar(new ResponsabilidadUsuario(responsable.id(), false, Set.of(CategoriaResponsable.DEPORTES)));
        mvc.perform(get("/api/v1/reservas").header("Authorization", token)).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/reservas/"+solicitud.id().value()).header("Authorization", token))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("AREA_NO_AUTORIZADA"));
        mvc.perform(post(ruta("aceptar")).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"operadorId\":\""+responsable.id().value()+"\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("AREA_NO_AUTORIZADA"));
        mvc.perform(post(ruta("denegar")).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"operadorId\":\""+responsable.id().value()+"\",\"motivo\":\"No corresponde\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("AREA_NO_AUTORIZADA"));
    }
    @Test void alumnoSoloVeSusSolicitudesYPublicoNoVeDatos() throws Exception {
        mvc.perform(get("/api/v1/reservas")).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/reservas").header("Authorization", token(alumno)))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(solicitud.id().value().toString()));
        mvc.perform(get("/api/v1/reservas").header("Authorization", token(usuarios.guardar(Fixtures.alumnoValidado()))))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test void cancelarAprobadaLiberaElEjemplar() throws Exception {
        aprobar();
        mvc.perform(post(ruta("cancelar")).header("Authorization", token(alumno)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"prestatarioId\":\""+alumno.id().value()+"\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADA"));
        em.flush(); em.clear();
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estaDisponible()).isTrue();
        assertThat(reservas.buscarPorId(solicitud.id()).orElseThrow().operadorId()).isEqualTo(responsable.id());
    }
    @Test void plazoVencidoLiberaElEjemplarSinCrearPrestamo() {
        var ahora=clock.instant();
        solicitud.aprobar(responsable.id(), ejemplar.id(), ahora.minus(Duration.ofHours(49)), ahora.minusSeconds(1));
        reservas.guardar(solicitud); ejemplar.reservar(); ejemplares.guardar(ejemplar); em.flush(); em.clear();
        expirar.ejecutar(solicitud.id()); em.flush(); em.clear();
        assertThat(reservas.buscarPorId(solicitud.id()).orElseThrow().estado()).isEqualTo(EstadoReserva.EXPIRADA);
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estaDisponible()).isTrue();
        assertThat(prestamos.existeActivoPara(ejemplar.id())).isFalse();
    }
    @Test void otroAlumnoNoPuedeApropiarseDelEjemplarApartado() throws Exception {
        aprobar();
        var otro=usuarios.guardar(Fixtures.alumnoValidado());
        var segunda=reservas.guardar(Reserva.solicitar(otro.id(), ejemplar.catalogoItemId()));
        mvc.perform(post("/api/v1/reservas/"+segunda.id().value()+"/aceptar").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"operadorId\":\""+responsable.id().value()+"\",\"ejemplarId\":\""+ejemplar.id().value()+"\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("EJEMPLAR_NO_DISPONIBLE"));
    }
    @Test void entregaVuelveAValidarLaCuentaDelAlumno() throws Exception {
        aprobar(); alumno.suspender("Pendiente de regularizacion"); usuarios.guardar(alumno);
        mvc.perform(post(ruta("entregar")).header("Authorization", token))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("USUARIO_NO_HABILITADO"));
    }
    @Test void rechazarExigeMotivoYNoApartaElEjemplar() throws Exception {
        mvc.perform(post(ruta("denegar")).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"operadorId\":\""+responsable.id().value()+"\",\"motivo\":\"Uso solicitado no autorizado\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("DENEGADA"));
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estaDisponible()).isTrue();
    }
}
