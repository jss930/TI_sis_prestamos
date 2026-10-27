package com.biblioteca.infrastructure.web;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.model.*;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.infrastructure.security.TokenService;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.support.Fixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ResponsablesInspeccionesTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EjemplarRepository ejemplares;
    @Autowired PrestamoRepository prestamos;
    @Autowired com.biblioteca.reservations.domain.repository.ReservaRepository reservas;
    @Autowired TokenService tokens;
    @Autowired EntityManager em;
    private Usuario admin;
    private Usuario alumno;
    private Ejemplar ejemplar;
    private Prestamo prestamo;
    private String token;

    @BeforeEach
    void preparar() {
        admin = usuarios.guardar(Fixtures.bibliotecario());
        alumno = usuarios.guardar(Fixtures.alumnoValidado());
        var item = catalogo.guardar(Fixtures.laptop());
        ejemplar = Fixtures.ejemplarDisponible(item.id());
        ejemplar.prestar();
        ejemplares.guardar(ejemplar);
        prestamo = prestamos.guardar(Prestamo.registrar(admin.id(), alumno.id(), ejemplar.id(),
                new PeriodoPrestamo(LocalDate.now().minusDays(1), LocalDate.now().plusDays(7)), LugarUso.EN_CAMPUS));
        token = "Bearer " + tokens.generar(admin.id().value(), admin.email().value());
        em.flush();
        em.clear();
    }

    private String rutaResponsable() { return "/api/v1/responsables/" + alumno.id().value(); }
    private String rutaInspeccion() { return "/api/v1/prestamos/" + prestamo.id().value() + "/inspeccion-devolucion"; }

    @Test
    void endpointsNuevosExigenSesionValida() throws Exception {
        mvc.perform(get("/api/v1/responsables")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/inspecciones-devolucion").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put(rutaResponsable() + "/capacitacion").contentType(MediaType.APPLICATION_JSON)
                .content("{\"verificada\":true}" )).andExpect(status().isUnauthorized());
        mvc.perform(post(rutaInspeccion()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"estadoFisico\":\"DISPONIBLE\",\"accesoriosFaltantes\":false}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unaCuentaSinPermisosNoAdministraResponsables() throws Exception {
        String sinPermisos = "Bearer " + tokens.generar(alumno.id().value(), alumno.email().value());
        mvc.perform(put(rutaResponsable() + "/capacitacion").header("Authorization", sinPermisos)
                .contentType(MediaType.APPLICATION_JSON).content("{\"verificada\":true}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("OPERADOR_NO_HABILITADO"));
    }


    @Test
    void confirmarGuardaLaSeleccionCompletaSinDuplicarHistorial() throws Exception {
        String seleccion = "{\"capacitacionVerificada\":true,\"categorias\":[\"BIBLIOTECA\",\"TECNOLOGIA\",\"COMPUTACION\"]}";
        for (int intento = 0; intento < 2; intento++) {
            mvc.perform(put(rutaResponsable() + "/asignaciones").header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON).content(seleccion))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.responsables[0].capacitacionVerificada").value(true))
                    .andExpect(jsonPath("$.responsables[0].categorias.length()").value(3))
                    .andExpect(jsonPath("$.historial.length()").value(4));
        }
        em.flush(); em.clear();
        mvc.perform(get("/api/v1/responsables").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsables[0].categorias.length()").value(3));
        mvc.perform(put(rutaResponsable() + "/asignaciones").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"capacitacionVerificada\":false,\"categorias\":[\"DEPORTES\"]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsables[0].capacitacionVerificada").value(false))
                .andExpect(jsonPath("$.responsables[0].categorias.length()").value(1))
                .andExpect(jsonPath("$.responsables[0].categorias[0]").value("DEPORTES"));
    }

    @Test
    void seleccionInvalidaNoGuardaNingunaAsignacion() throws Exception {
        mvc.perform(put(rutaResponsable() + "/asignaciones").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"capacitacionVerificada\":false,\"categorias\":[\"BIBLIOTECA\",\"TECNOLOGIA\"]}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CAPACITACION_REQUERIDA"));
        em.clear();
        assertThat(em.createQuery("select count(e) from CambioResponsableJpaEntity e", Long.class).getSingleResult()).isZero();
        assertThat(em.createQuery("select count(e) from ResponsabilidadUsuarioJpaEntity e", Long.class).getSingleResult()).isZero();
    }

    @Test
    void confirmarResponsablesExigeAdministradorYCamposValidos() throws Exception {
        String seleccion = "{\"capacitacionVerificada\":false,\"categorias\":[\"BIBLIOTECA\"]}";
        mvc.perform(put(rutaResponsable() + "/asignaciones").contentType(MediaType.APPLICATION_JSON).content(seleccion))
                .andExpect(status().isUnauthorized());
        String tokenAlumno = "Bearer " + tokens.generar(alumno.id().value(), alumno.email().value());
        mvc.perform(put(rutaResponsable() + "/asignaciones").header("Authorization", tokenAlumno)
                .contentType(MediaType.APPLICATION_JSON).content(seleccion))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("OPERADOR_NO_HABILITADO"));
        mvc.perform(put(rutaResponsable() + "/asignaciones").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }


    private void confirmarCargo(Usuario persona, String cargo) throws Exception {
        mvc.perform(put("/api/v1/responsables/" + persona.id().value() + "/asignaciones").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"capacitacionVerificada":false,"categorias":["BIBLIOTECA"],"cargos":{"BIBLIOTECA":"%s"}}
                """.formatted(cargo)))
                .andExpect(status().isOk());
        em.flush(); em.clear();
    }

    @Test
    void soloPermiteUnPrincipalYUnSuplentePorArea() throws Exception {
        var suplente = usuarios.guardar(Fixtures.alumnoValidado());
        var tercera = usuarios.guardar(Fixtures.alumnoValidado());
        confirmarCargo(alumno, "PRINCIPAL");
        confirmarCargo(suplente, "SUPLENTE");
        mvc.perform(get("/api/v1/responsables").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsables.length()").value(2));
        for (String cargo : new String[]{"PRINCIPAL", "SUPLENTE"}) {
            mvc.perform(put("/api/v1/responsables/" + tercera.id().value() + "/asignaciones").header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON).content("""
                    {"capacitacionVerificada":false,"categorias":["BIBLIOTECA"],"cargos":{"BIBLIOTECA":"%s"}}
                    """.formatted(cargo)))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CARGO_OCUPADO"));
        }
        assertThat(em.createQuery("select count(e) from CambioResponsableJpaEntity e", Long.class).getSingleResult()).isEqualTo(2);
    }

    @Test
    void noPermiteOtroPrincipalAunqueElSuplenteEsteLibre() throws Exception {
        confirmarCargo(alumno, "PRINCIPAL");
        var otra = usuarios.guardar(Fixtures.alumnoValidado());
        mvc.perform(put("/api/v1/responsables/" + otra.id().value() + "/categorias/BIBLIOTECA").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"asignada\":true,\"cargo\":\"PRINCIPAL\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CARGO_OCUPADO"));
    }

    @Test
    void cambiarCargoSeConservaYQuedaEnElHistorial() throws Exception {
        confirmarCargo(alumno, "PRINCIPAL");
        confirmarCargo(alumno, "SUPLENTE");
        mvc.perform(get("/api/v1/responsables").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsables[0].cargos.BIBLIOTECA").value("SUPLENTE"))
                .andExpect(jsonPath("$.historial[0].accion").value("CARGO_CAMBIADO"))
                .andExpect(jsonPath("$.historial[0].cargo").value("SUPLENTE"));
    }

    @Test
    void retirarPrincipalLiberaElCargoParaOtraPersona() throws Exception {
        confirmarCargo(alumno, "PRINCIPAL");
        mvc.perform(put(rutaResponsable() + "/asignaciones").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"capacitacionVerificada\":false,\"categorias\":[],\"cargos\":{}}"))
                .andExpect(status().isOk());
        em.flush(); em.clear();
        var nueva = usuarios.guardar(Fixtures.alumnoValidado());
        confirmarCargo(nueva, "PRINCIPAL");
        assertThat(usuarios.buscarPorId(alumno.id()).orElseThrow().puedeGestionarReservas()).isFalse();
    }

    @Test
    void cargosDebenCoincidirConLasAreasSeleccionadas() throws Exception {
        mvc.perform(put(rutaResponsable() + "/asignaciones").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"capacitacionVerificada\":false,\"categorias\":[\"BIBLIOTECA\"],\"cargos\":{}}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CARGOS_INCONSISTENTES"));
    }

    @Test
    void categoriaTecnicaRequiereCapacitacion() throws Exception {
        mvc.perform(put(rutaResponsable() + "/categorias/TECNOLOGIA").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"asignada\":true}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CAPACITACION_REQUERIDA"));
        assertThat(em.createQuery("select count(e) from CambioResponsableJpaEntity e", Long.class).getSingleResult()).isZero();
    }

    @Test
    void capacitacionAsignacionesYAuditoriaSeConservanAlRecargar() throws Exception {
        mvc.perform(put(rutaResponsable() + "/capacitacion").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"verificada\":true}"))
                .andExpect(status().isOk());
        for (String categoria : new String[]{"BIBLIOTECA", "TECNOLOGIA", "COMPUTACION"}) {
            mvc.perform(put(rutaResponsable() + "/categorias/" + categoria).header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"asignada\":true}"))
                    .andExpect(status().isOk());
        }
        // Repetir el mismo PUT no duplica asignaciones ni auditoria.
        mvc.perform(put(rutaResponsable() + "/categorias/TECNOLOGIA").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"asignada\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.historial.length()").value(4));
        em.flush(); em.clear();
        mvc.perform(get("/api/v1/responsables").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsables[0].categorias.length()").value(3))
                .andExpect(jsonPath("$.historial[0].operadorId").value(admin.id().value().toString()));
        mvc.perform(put(rutaResponsable() + "/capacitacion").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"verificada\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsables[0].capacitacionVerificada").value(false))
                .andExpect(jsonPath("$.responsables[0].categorias[0]").value("BIBLIOTECA"))
                .andExpect(jsonPath("$.responsables[0].categorias.length()").value(1))
                .andExpect(jsonPath("$.historial.length()").value(7));
        em.flush(); em.clear();
        mvc.perform(get("/api/v1/responsables").header("Authorization", token))
                .andExpect(jsonPath("$.historial.length()").value(7));
    }

    @Test
    void usuarioSuspendidoNoRecibeAsignaciones() throws Exception {
        alumno.suspender("Cuenta suspendida de prueba");
        usuarios.guardar(alumno); em.flush(); em.clear();
        mvc.perform(put(rutaResponsable() + "/categorias/BIBLIOTECA").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"asignada\":true}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("USUARIO_NO_VALIDADO"));
    }

    @Test
    void sePuedenRetirarAsignacionesDeUsuarioSuspendido() throws Exception {
        mvc.perform(put(rutaResponsable() + "/categorias/BIBLIOTECA").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"asignada\":true}"))
                .andExpect(status().isOk());
        alumno.suspender("Cuenta suspendida de prueba");
        usuarios.guardar(alumno); em.flush(); em.clear();
        mvc.perform(put(rutaResponsable() + "/categorias/BIBLIOTECA").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"asignada\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsables[0].categorias.length()").value(0));
    }

    @Test
    void camposObligatoriosSeValidan() throws Exception {
        mvc.perform(put(rutaResponsable() + "/capacitacion").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post(rutaInspeccion()).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"estadoFisico\":\"DISPONIBLE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accesoriosFaltantesExigenObservacionSinCerrarElPrestamo() throws Exception {
        mvc.perform(post(rutaInspeccion()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"estadoFisico\":\"DISPONIBLE\",\"accesoriosFaltantes\":true,\"observaciones\":\"   \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("OBSERVACIONES_REQUERIDAS"));
        em.clear();
        assertThat(prestamos.buscarPorId(prestamo.id()).orElseThrow().estado()).isEqualTo(EstadoPrestamo.ACTIVO);
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estadoFisico()).isEqualTo(EstadoFisico.PRESTADO);
    }

    @Test
    void recepcionConFaltantesGuardaAuditoriaYBloqueaNuevoPrestamo() throws Exception {
        mvc.perform(post(rutaInspeccion()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"estadoFisico\":\"DISPONIBLE\",\"accesoriosFaltantes\":true,\"observaciones\":\"Falta el cargador\",\"recibidoPorId\":\"" + alumno.id().value() + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estadoResultante").value("EN_REVISION"))
                .andExpect(jsonPath("$.recibidoPorId").value(admin.id().value().toString()));
        em.flush(); em.clear();
        assertThat(prestamos.buscarPorId(prestamo.id()).orElseThrow().estado()).isEqualTo(EstadoPrestamo.DEVUELTO);
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estaDisponible()).isFalse();
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estadoFisico()).isEqualTo(EstadoFisico.EN_REVISION);
        mvc.perform(get("/api/v1/inspecciones-devolucion").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].observaciones").value("Falta el cargador"))
                .andExpect(jsonPath("$[0].accesoriosFaltantes").value(true));
        mvc.perform(post("/api/v1/prestamos").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("""
                {"operadorId":"%s","prestatarioId":"%s","ejemplarId":"%s","lugarUso":"EN_CAMPUS"}
                """.formatted(admin.id().value(), alumno.id().value(), ejemplar.id().value())))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("EJEMPLAR_NO_DISPONIBLE"));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoFisico.class, names = {"DISPONIBLE", "DANADO", "PERDIDO"})
    void conservaElEstadoFisicoDeCadaRecepcion(EstadoFisico estado) throws Exception {
        mvc.perform(post(rutaInspeccion()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"estadoFisico\":\"" + estado + "\",\"accesoriosFaltantes\":false,\"observaciones\":\"Inspeccion de prueba\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estadoResultante").value(estado.name()));
        em.flush(); em.clear();
        assertThat(ejemplares.buscarPorId(ejemplar.id()).orElseThrow().estadoFisico()).isEqualTo(estado);
        assertThat(prestamos.buscarPorId(prestamo.id()).orElseThrow().estado())
                .isEqualTo(estado == EstadoFisico.PERDIDO ? EstadoPrestamo.PERDIDO : EstadoPrestamo.DEVUELTO);
    }

    @Test
    void noPermiteRegistrarDosInspeccionesDelMismoPrestamo() throws Exception {
        String datos = "{\"estadoFisico\":\"DISPONIBLE\",\"accesoriosFaltantes\":false}";
        mvc.perform(post(rutaInspeccion()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isOk());
        em.flush(); em.clear();
        mvc.perform(post(rutaInspeccion()).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(datos))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("PRESTAMO_NO_ACTIVO"));
        assertThat(em.createQuery("select count(e) from InspeccionDevolucionJpaEntity e", Long.class).getSingleResult()).isEqualTo(1);
    }

    @Test
    void prestatarioNoPuedeInspeccionarSuPropioPrestamoSinPermiso() throws Exception {
        String sinPermiso = "Bearer " + tokens.generar(alumno.id().value(), alumno.email().value());
        mvc.perform(post(rutaInspeccion()).header("Authorization", sinPermiso).contentType(MediaType.APPLICATION_JSON)
                .content("{\"estadoFisico\":\"DISPONIBLE\",\"accesoriosFaltantes\":false}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("OPERADOR_NO_HABILITADO"));
    }
    @Test
    void aceptaReservaConElEjemplarElegido() throws Exception {
        var copia1 = ejemplares.guardar(Fixtures.ejemplarDisponible(ejemplar.catalogoItemId()));
        var elegida = ejemplares.guardar(Fixtures.ejemplarDisponible(ejemplar.catalogoItemId()));
        var reserva = reservas.guardar(com.biblioteca.reservations.domain.model.Reserva.solicitar(alumno.id(), ejemplar.catalogoItemId()));
        mvc.perform(post("/api/v1/reservas/" + reserva.id().value() + "/aceptar").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"operadorId":"%s","ejemplarId":"%s"}
                """.formatted(admin.id().value(), elegida.id().value())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("APROBADA"));
        em.flush(); em.clear();
        var aceptada = reservas.buscarPorId(reserva.id()).orElseThrow();
        assertThat(aceptada.prestamoId()).isNull();
        assertThat(aceptada.ejemplarId()).isEqualTo(elegida.id());
        assertThat(ejemplares.buscarPorId(elegida.id()).orElseThrow().estadoFisico()).isEqualTo(EstadoFisico.RESERVADO);
        assertThat(ejemplares.buscarPorId(copia1.id()).orElseThrow().estaDisponible()).isTrue();
    }

    @Test
    void rechazaEjemplarDeOtroBienSinAceptarLaReserva() throws Exception {
        var otroBien = catalogo.guardar(Fixtures.libro());
        var otroEjemplar = ejemplares.guardar(Fixtures.ejemplarDisponible(otroBien.id()));
        var reserva = reservas.guardar(com.biblioteca.reservations.domain.model.Reserva.solicitar(alumno.id(), ejemplar.catalogoItemId()));
        em.flush();
        mvc.perform(post("/api/v1/reservas/" + reserva.id().value() + "/aceptar").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"operadorId":"%s","ejemplarId":"%s"}
                """.formatted(admin.id().value(), otroEjemplar.id().value())))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("EJEMPLAR_NO_DISPONIBLE"));
        em.clear();
        assertThat(reservas.buscarPorId(reserva.id()).orElseThrow().estado().name()).isEqualTo("SOLICITADA");
        assertThat(ejemplares.buscarPorId(otroEjemplar.id()).orElseThrow().estaDisponible()).isTrue();
    }

    @Test
    void conservaAceptacionSinEjemplarExplicito() throws Exception {
        ejemplares.guardar(Fixtures.ejemplarDisponible(ejemplar.catalogoItemId()));
        var reserva = reservas.guardar(com.biblioteca.reservations.domain.model.Reserva.solicitar(alumno.id(), ejemplar.catalogoItemId()));
        mvc.perform(post("/api/v1/reservas/" + reserva.id().value() + "/aceptar").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"operadorId\":\"" + admin.id().value() + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("APROBADA"));
    }

}
