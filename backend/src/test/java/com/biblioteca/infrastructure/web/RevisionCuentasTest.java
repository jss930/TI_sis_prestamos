package com.biblioteca.infrastructure.web;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.identity.domain.model.*;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.*;
import com.biblioteca.infrastructure.security.TokenService;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
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
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RevisionCuentasTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EjemplarRepository ejemplares;
    @Autowired TokenService tokens;
    @Autowired EntityManager em;
    Usuario admin, cuenta;
    String tokenAdmin, tokenCuenta;

    @BeforeEach void preparar() {
        admin=usuarios.guardar(Fixtures.bibliotecario());
        cuenta=Fixtures.alumnoProvisional();
        cuenta.actualizarDatosPendientes(cuenta.email(),cuenta.nombreCompleto(),cuenta.perfil(),new CodigoInstitucional("ALU-"+UUID.randomUUID().toString().substring(0,8)));
        usuarios.guardar(cuenta);
        tokenAdmin=token(admin); tokenCuenta=token(cuenta);
        em.flush(); em.clear();
    }
    String token(Usuario usuario) { return "Bearer "+tokens.generar(usuario.id().value(),usuario.email().value()); }
    String ruta(String accion) { return "/api/v1/usuarios/"+cuenta.id().value()+"/"+accion; }
    String datos(String codigo) {
        return """
            {"email":"%s","nombreCompleto":"Nombre corregido","perfil":"ALUMNO","codigoInstitucional":"%s"}
            """.formatted(cuenta.email().value(),codigo);
    }
    void observar() throws Exception {
        mvc.perform(post(ruta("solicitar-correccion")).header("Authorization",tokenAdmin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"observaciones\":\"Corrige el codigo institucional segun tu documento\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.correccionPendiente").value(true));
        em.flush(); em.clear();
    }
    @Test void validarConservaQuienValidoYCuandoSinOtorgarPermisos() throws Exception {
        mvc.perform(post(ruta("validar")).header("Authorization",tokenAdmin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"datosRevisados\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("VALIDADO"))
            .andExpect(jsonPath("$.validadoPorId").value(admin.id().value().toString()))
            .andExpect(jsonPath("$.fechaValidacion").isNotEmpty()).andExpect(jsonPath("$.permisos").isEmpty());
        em.flush(); em.clear();
        var guardada=usuarios.buscarPorId(cuenta.id()).orElseThrow();
        assertThat(guardada.validadoPorId()).isEqualTo(admin.id());
        assertThat(guardada.fechaValidacion()).isNotNull();
        assertThat(guardada.puedeSolicitarPrestamo()).isTrue();
    }
    @Test void administradorPuedeValidarSinUnPermisoAdicional() throws Exception {
        var administrador=Fixtures.alumnoValidado(); administrador.otorgarPermiso(PermisoOperativo.ADMINISTRAR_SISTEMA);
        usuarios.guardar(administrador);
        mvc.perform(post(ruta("validar")).header("Authorization",token(administrador)).contentType(MediaType.APPLICATION_JSON)
            .content("{\"datosRevisados\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.validadoPorId").value(administrador.id().value().toString()));
    }
    @Test void validarExigeConfirmarLaRevision() throws Exception {
        mvc.perform(post(ruta("validar")).header("Authorization",tokenAdmin).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        assertThat(usuarios.buscarPorId(cuenta.id()).orElseThrow().estado()).isEqualTo(EstadoCuentaUsuario.PROVISIONAL);
    }
    @Test void cuentaHistoricaSinCodigoNoSeValidaHastaCompletarlo() throws Exception {
        var anterior=usuarios.guardar(Fixtures.alumnoProvisional());
        mvc.perform(post("/api/v1/usuarios/"+anterior.id().value()+"/validar").header("Authorization",tokenAdmin)
            .contentType(MediaType.APPLICATION_JSON).content("{\"datosRevisados\":true}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CODIGO_INSTITUCIONAL_REQUERIDO"));
    }
    @Test void soloUnRevisorAutorizadoPuedeValidarOSolicitarCorrecciones() throws Exception {
        mvc.perform(post(ruta("validar")).header("Authorization",tokenCuenta).contentType(MediaType.APPLICATION_JSON).content("{\"datosRevisados\":true}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("AUTOVALIDACION_NO_PERMITIDA"));
        mvc.perform(post(ruta("solicitar-correccion")).header("Authorization",tokenCuenta)
            .contentType(MediaType.APPLICATION_JSON).content("{\"observaciones\":\"Cambio de datos\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("OPERADOR_NO_HABILITADO"));
        mvc.perform(post(ruta("validar")).contentType(MediaType.APPLICATION_JSON).content("{\"datosRevisados\":true}"))
            .andExpect(status().isUnauthorized());
    }
    @Test void observacionesExigenTextoYQuedanVisiblesSoloAlPropietarioYRevisores() throws Exception {
        observar();
        mvc.perform(get("/api/v1/usuarios/"+cuenta.id().value()).header("Authorization",tokenCuenta))
            .andExpect(status().isOk()).andExpect(jsonPath("$.observacionesRevision").isNotEmpty())
            .andExpect(jsonPath("$.revisadoPorId").value(admin.id().value().toString()));
        mvc.perform(get("/api/v1/usuarios/"+cuenta.id().value()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.observacionesRevision").isEmpty()).andExpect(jsonPath("$.codigoInstitucional").isEmpty());
        var tercero=usuarios.guardar(Fixtures.alumnoValidado());
        mvc.perform(get("/api/v1/usuarios/"+cuenta.id().value()).header("Authorization",token(tercero)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.observacionesRevision").isEmpty());
        mvc.perform(post(ruta("solicitar-correccion")).header("Authorization",tokenAdmin)
            .contentType(MediaType.APPLICATION_JSON).content("{\"observaciones\":\" \"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void propietarioCorrigeYReenviaAntesDeLaValidacion() throws Exception {
        observar();
        mvc.perform(post(ruta("validar")).header("Authorization",tokenAdmin).contentType(MediaType.APPLICATION_JSON).content("{\"datosRevisados\":true}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CORRECCION_PENDIENTE"));
        mvc.perform(patch(ruta("datos-pendientes")).header("Authorization",tokenCuenta).contentType(MediaType.APPLICATION_JSON).content(datos("20241234")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.correccionPendiente").value(false))
            .andExpect(jsonPath("$.estado").value("PROVISIONAL")).andExpect(jsonPath("$.nombreCompleto").value("Nombre corregido"));
        em.flush(); em.clear();
        mvc.perform(post(ruta("validar")).header("Authorization",tokenAdmin).contentType(MediaType.APPLICATION_JSON).content("{\"datosRevisados\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("VALIDADO"));
    }
    @Test void noSePuedenModificarDatosValidadosNiLosDeOtraPersona() throws Exception {
        var otro=usuarios.guardar(Fixtures.alumnoValidado());
        mvc.perform(patch(ruta("datos-pendientes")).header("Authorization",token(otro)).contentType(MediaType.APPLICATION_JSON).content(datos("20241234")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("OPERADOR_NO_HABILITADO"));
        cuenta.validar(); usuarios.guardar(cuenta); em.flush(); em.clear();
        mvc.perform(patch(ruta("datos-pendientes")).header("Authorization",tokenCuenta).contentType(MediaType.APPLICATION_JSON).content(datos("20241234")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("TRANSICION_INVALIDA"));
    }
    @Test void codigoInstitucionalDebeSerValidoYUnico() throws Exception {
        var otra=Fixtures.alumnoProvisional();
        otra.actualizarDatosPendientes(otra.email(),otra.nombreCompleto(),otra.perfil(),new CodigoInstitucional("20241234")); usuarios.guardar(otra);
        mvc.perform(patch(ruta("datos-pendientes")).header("Authorization",tokenCuenta).contentType(MediaType.APPLICATION_JSON).content(datos("20241234")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CODIGO_INSTITUCIONAL_DUPLICADO"));
        mvc.perform(patch(ruta("datos-pendientes")).header("Authorization",tokenCuenta).contentType(MediaType.APPLICATION_JSON).content(datos("??")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("CODIGO_INSTITUCIONAL_INVALIDO"));
    }
    @Test void registroNuevoQuedaPendienteYExigeCodigo() throws Exception {
        String correo="nueva-"+UUID.randomUUID()+"@unsa.edu.pe";
        String registro="""
            {"email":"%s","nombreCompleto":"Nueva Alumna","perfil":"ALUMNO","password":"ClaveDePrueba123","codigoInstitucional":"20241234"}
            """.formatted(correo);
        mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content(registro))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("PROVISIONAL"))
            .andExpect(jsonPath("$.codigoInstitucional").value("20241234"));
        mvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON).content(registro.replace("20241234", "")))
            .andExpect(status().isBadRequest());
    }
    @Test void cuentaPendienteNoPuedeSolicitarAunqueTengaCredenciales() throws Exception {
        var item=catalogo.guardar(Fixtures.laptop()); ejemplares.guardar(Fixtures.ejemplarDisponible(item.id()));
        var inicio=java.time.Instant.now().plus(java.time.Duration.ofHours(3));
        var fin=inicio.plus(java.time.Duration.ofHours(1));
        mvc.perform(post("/api/v1/reservas").header("Authorization",tokenCuenta).contentType(MediaType.APPLICATION_JSON)
            .content("{\"prestatarioId\":\""+cuenta.id().value()+"\",\"catalogoItemId\":\""+item.id().value()
                    +"\",\"inicioReserva\":\""+inicio+"\",\"finReserva\":\""+fin+"\"}"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("USUARIO_NO_HABILITADO"));
    }
}
