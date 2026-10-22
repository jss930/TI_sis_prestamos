package com.biblioteca.identity.domain.model;

import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.NombreCompleto;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRolAccesoTest {

    @Test
    void alumnoValidadoSinPermisosEsEstudiante() {
        var usuario = Fixtures.alumnoValidado();
        assertThat(usuario.rolAcceso()).isEqualTo(RolAcceso.ESTUDIANTE);
    }

    @Test
    void docenteValidadoEsDocente() {
        var usuario = Fixtures.docenteValidado();
        assertThat(usuario.rolAcceso()).isEqualTo(RolAcceso.DOCENTE);
    }

    @Test
    void administrativoValidadoSinPermisosEsAdministrativo() {
        var usuario = Usuario.reconstruir(
                new UsuarioId(UUID.randomUUID()),
                new EmailAddress("admin-" + UUID.randomUUID() + "@universidad.edu.pe"),
                new NombreCompleto("Ana Administrativa"),
                PerfilPrestatario.ADMINISTRATIVO,
                EstadoCuentaUsuario.VALIDADO,
                EnumSet.noneOf(PermisoOperativo.class),
                Instant.now().minusSeconds(10_000),
                Instant.now().minusSeconds(5_000),
                null);
        assertThat(usuario.rolAcceso()).isEqualTo(RolAcceso.ADMINISTRATIVO);
    }

    @Test
    void cuentaValidadaConPermisoDeGestionEsAdministrador() {
        var usuario = Fixtures.bibliotecario();
        assertThat(usuario.rolAcceso()).isEqualTo(RolAcceso.ADMINISTRADOR);
    }

    @Test
    void cuentaProvisionalConPermisosNoEsAdministrador() {
        var usuario = Fixtures.provisionalConPermisoDeOperar();
        assertThat(usuario.rolAcceso()).isNotEqualTo(RolAcceso.ADMINISTRADOR);
    }

    @Test
    void cuentaSuspendidaConPermisosNoEsAdministrador() {
        var usuario = Fixtures.bibliotecario();
        usuario.suspender("Motivo");
        assertThat(usuario.rolAcceso()).isNotEqualTo(RolAcceso.ADMINISTRADOR);
    }
}