package com.biblioteca.infrastructure.web;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.policy.*;
import com.biblioteca.circulation.domain.repository.*;
import com.biblioteca.identity.domain.repository.*;
import com.biblioteca.identity.domain.vo.*;
import com.biblioteca.infrastructure.config.CargaDemoAmpliada;
import com.biblioteca.infrastructure.config.DemoDataConfig;
import com.biblioteca.infrastructure.security.PasswordHasher;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.support.Fixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CargaDemoAmpliadaTest {
    @Autowired CargaDemoAmpliada carga;
    @Autowired UsuarioRepository usuarios;
    @Autowired CredencialRepository credenciales;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EjemplarRepository ejemplares;
    @Autowired PrestamoRepository prestamos;
    @Autowired ReservaRepository reservas;
    @Autowired SancionRepository sanciones;
    @Autowired InspeccionDevolucionRepository inspecciones;
    @Autowired PasswordHasher passwords;
    @Autowired JdbcTemplate jdbc;

    @Test void aditivaCoherenteEIdempotente() {
        var existente = usuarios.guardar(Fixtures.alumnoValidado());
        var original = existente.nombreCompleto();
        long cuentasAntes = usuarios.listarTodos().size();
        long prestamosAntes = prestamos.listarTodos().size();
        long copiasAntes = ejemplares.listarTodos().size();
        assertThat(carga.cargar()).isTrue();
        assertThat(usuarios.listarTodos()).hasSize((int) cuentasAntes + 8);
        assertThat(prestamos.listarTodos()).hasSize((int) prestamosAntes + 40);
        assertThat(ejemplares.listarTodos()).hasSize((int) copiasAntes + 18);
        assertThat(reservas.listarTodos()).hasSize(6);
        assertThat(inspecciones.listar()).hasSize(36);
        assertThat(usuarios.buscarPorId(existente.id()).orElseThrow().nombreCompleto()).isEqualTo(original);
        var operador = usuarios.buscarPorId(new UsuarioId(CargaDemoAmpliada.id("usuario", 0))).orElseThrow();
        assertThat(operador.puedeAdministrarSistema()).isTrue();
        assertThat(operador.puedeOperarVentanilla()).isTrue();
        var hash = credenciales.buscarHashPorUsuario(operador.id()).orElseThrow();
        assertThat(passwords.matches("password", hash)).isTrue();
        for (var p : prestamos.listarTodos()) {
            var copia = ejemplares.buscarPorId(p.ejemplarId()).orElseThrow();
            var item = catalogo.buscarPorId(copia.catalogoItemId()).orElseThrow();
            var persona = usuarios.buscarPorId(p.prestatarioId()).orElseThrow();
            assertThat(p.operadorId()).isNotEqualTo(p.prestatarioId());
            PoliticaPlazoPrestamo.validar(p.fechaHoraEntrega(), p.fechaHoraVencimiento(), p.lugarUso(),
                    persona.perfil(), item.tipo(), new PoliticaPrestamoEstandar());
        }
        long numero = prestamos.listarTodos().size();
        assertThat(carga.cargar()).isFalse();
        assertThat(prestamos.listarTodos()).hasSize((int) numero);
        assertThat(credenciales.buscarHashPorUsuario(operador.id()).orElseThrow()).isEqualTo(hash);
        assertThat(jdbc.queryForObject("SELECT completada FROM cargas_demo WHERE clave='ampliada-v1'", Boolean.class)).isTrue();
    }

    @Test void colisionDeCorreoNuncaSobrescribeNiCargaParcialmente() {
        var cuenta = com.biblioteca.identity.domain.model.Usuario.registrarProvisional(
                new EmailAddress("operador.demo@universidad.edu.pe"), new NombreCompleto("Cuenta preexistente"),
                com.biblioteca.identity.domain.model.PerfilPrestatario.ALUMNO);
        usuarios.guardar(cuenta);
        long antes = usuarios.listarTodos().size();
        assertThat(carga.cargar()).isFalse();
        assertThat(usuarios.listarTodos()).hasSize((int) antes);
        assertThat(usuarios.buscarPorId(cuenta.id()).orElseThrow().nombreCompleto().value()).isEqualTo("Cuenta preexistente");
        assertThat(jdbc.queryForObject("SELECT completada FROM cargas_demo WHERE clave='ampliada-v1'", Boolean.class)).isFalse();
    }

    @Test void administradorRecibePrestamosDeOtroOperadorSinAutoprestamo() {
        var admin = com.biblioteca.identity.domain.model.Usuario.reconstruir(new UsuarioId(DemoDataConfig.BIBLIOTECARIO_ID),
                new EmailAddress("bibliotecario@biblioteca.pe"), new NombreCompleto("Elena Bibliotecaria"),
                com.biblioteca.identity.domain.model.PerfilPrestatario.ADMINISTRATIVO,
                com.biblioteca.identity.domain.model.EstadoCuentaUsuario.VALIDADO,
                EnumSet.allOf(com.biblioteca.identity.domain.model.PermisoOperativo.class),
                Instant.now().minus(Duration.ofDays(200)), Instant.now().minus(Duration.ofDays(190)), null);
        usuarios.guardar(admin);
        assertThat(carga.cargar()).isTrue();
        assertThat(prestamos.listarPorPrestatario(admin.id())).isNotEmpty().allSatisfy(p ->
                assertThat(p.operadorId()).isNotEqualTo(admin.id()));
        assertThat(prestamos.listarPorPrestatario(admin.id())).anySatisfy(p ->
                assertThat(p.estado()).isEqualTo(com.biblioteca.circulation.domain.model.EstadoPrestamo.ACTIVO));
    }
}
