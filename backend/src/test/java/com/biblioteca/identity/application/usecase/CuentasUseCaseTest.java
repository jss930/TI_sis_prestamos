package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.application.command.OtorgarPermisoCommand;
import com.biblioteca.identity.application.command.RevocarPermisoCommand;
import com.biblioteca.identity.application.command.ValidarUsuarioCommand;
import com.biblioteca.identity.domain.model.PermisoOperativo;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryUsuarioRepository;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.support.Fixtures;
import com.biblioteca.support.RecordingEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentasUseCaseTest {

    private UsuarioRepository usuarios;
    private RecordingEventPublisher eventos;
    private ValidarUsuarioUseCase validar;
    private SuspenderUsuarioUseCase suspender;
    private ReactivarUsuarioUseCase reactivar;
    private OtorgarPermisoUseCase otorgar;
    private RevocarPermisoUseCase revocar;

    private Usuario operador;
    private Usuario cuenta;

    @BeforeEach
    void setUp() {
        usuarios = new InMemoryUsuarioRepository();
        eventos = new RecordingEventPublisher();
        validar = new ValidarUsuarioUseCase(usuarios, eventos);
        suspender = new SuspenderUsuarioUseCase(usuarios, eventos);
        reactivar = new ReactivarUsuarioUseCase(usuarios, eventos);
        otorgar = new OtorgarPermisoUseCase(usuarios, eventos);
        revocar = new RevocarPermisoUseCase(usuarios);

        operador = Fixtures.bibliotecario();
        cuenta = Fixtures.alumnoProvisional();
        usuarios.guardar(operador);
        usuarios.guardar(cuenta);
    }

    @Test
    void noSePuedeValidarLaPropiaCuenta() {
        assertThatThrownBy(() -> validar.ejecutar(
                new ValidarUsuarioCommand(operador.id(), operador.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("AUTOVALIDACION_NO_PERMITIDA"));
    }

    @Test
    void validarUnaCuentaYaValidadaFalla() {
        Usuario otra = Fixtures.alumnoValidado();
        usuarios.guardar(otra);

        assertThatThrownBy(() -> validar.ejecutar(
                new ValidarUsuarioCommand(operador.id(), otra.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("TRANSICION_INVALIDA"));
    }

    @Test
    void validarUnaCuentaSuspendidaFalla() {
        cuenta.validar();
        usuarios.guardar(cuenta);
        suspender.ejecutar(new com.biblioteca.identity.application.command.SuspenderUsuarioCommand(
                operador.id(), cuenta.id(), "Motivo"));

        assertThatThrownBy(() -> validar.ejecutar(
                new ValidarUsuarioCommand(operador.id(), cuenta.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("TRANSICION_INVALIDA"));
    }

    @Test
    void noSePuedeOtorgarPermisoAUnoMismo() {
        assertThatThrownBy(() -> otorgar.ejecutar(
                new OtorgarPermisoCommand(operador.id(), operador.id(), PermisoOperativo.REGISTRAR_PRESTAMO)))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("AUTOOTORGAMIENTO_NO_PERMITIDO"));
    }

    @Test
    void noSePuedeRevocarPermisoAUnoMismo() {
        assertThatThrownBy(() -> revocar.ejecutar(
                new RevocarPermisoCommand(operador.id(), operador.id(), PermisoOperativo.REGISTRAR_PRESTAMO)))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("AUTOREVOCACION_NO_PERMITIDA"));
    }

    @Test
    void suspenderConservaLosPermisos() {
        Usuario conPermiso = Fixtures.alumnoValidado();
        conPermiso.otorgarPermiso(PermisoOperativo.REGISTRAR_PRESTAMO);
        usuarios.guardar(conPermiso);

        suspender.ejecutar(new com.biblioteca.identity.application.command.SuspenderUsuarioCommand(
                operador.id(), conPermiso.id(), "Motivo"));

        Usuario suspendido = usuarios.buscarPorId(conPermiso.id()).orElseThrow();
        assertThat(suspendido.estado()).isEqualTo(com.biblioteca.identity.domain.model.EstadoCuentaUsuario.SUSPENDIDO);
        assertThat(suspendido.tienePermiso(PermisoOperativo.REGISTRAR_PRESTAMO)).isTrue();
        assertThat(suspendido.puedeOperarVentanilla()).isFalse();
    }

    @Test
    void operadorSinPermisoNoValida() {
        Usuario sinPermiso = Fixtures.alumnoValidado();
        usuarios.guardar(sinPermiso);

        assertThatThrownBy(() -> validar.ejecutar(
                new ValidarUsuarioCommand(sinPermiso.id(), cuenta.id())))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("OPERADOR_NO_HABILITADO"));
    }
}