package com.biblioteca.identity.domain.model;

import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.support.Fixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    @Test
    void unaCuentaProvisionalPuedeConsultarPeroNoPrestarNiOperar() {
        var usuario = Fixtures.alumnoProvisional();

        assertThat(usuario.puedeConsultarCatalogo()).isTrue();
        assertThat(usuario.puedeSolicitarPrestamo()).isFalse();
        assertThat(usuario.puedeOperarVentanilla()).isFalse();
    }

    @Test
    void unPermisoNoSaltaLaReglaDeCuentaProvisional() {
        var usuario = Fixtures.provisionalConPermisoDeOperar();

        assertThat(usuario.tienePermiso(PermisoOperativo.REGISTRAR_PRESTAMO)).isTrue();
        assertThat(usuario.puedeOperarVentanilla()).isFalse();
    }

    @Test
    void validarHabilitaSolicitarPrestamos() {
        var usuario = Fixtures.alumnoProvisional();

        usuario.validar();

        assertThat(usuario.estado()).isEqualTo(EstadoCuentaUsuario.VALIDADO);
        assertThat(usuario.puedeSolicitarPrestamo()).isTrue();
    }

    @Test
    void noSePuedeValidarDosVeces() {
        var usuario = Fixtures.alumnoProvisional();
        usuario.validar();

        assertThatThrownBy(usuario::validar)
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("TRANSICION_INVALIDA"));
    }

    @Test
    void suspenderBloqueaYReactivarHabilita() {
        var usuario = Fixtures.alumnoValidado();

        usuario.suspender("Conducta");

        assertThat(usuario.estado()).isEqualTo(EstadoCuentaUsuario.SUSPENDIDO);
        assertThat(usuario.puedeSolicitarPrestamo()).isFalse();

        usuario.reactivar();

        assertThat(usuario.estado()).isEqualTo(EstadoCuentaUsuario.VALIDADO);
        assertThat(usuario.puedeSolicitarPrestamo()).isTrue();
    }

    @Test
    void suspenderExigeMotivo() {
        var usuario = Fixtures.alumnoValidado();

        assertThatThrownBy(() -> usuario.suspender("  "))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void otorgarPermisoHabilitaVentanillaSoloSiEstaValidado() {
        var usuario = Fixtures.alumnoValidado();

        assertThat(usuario.puedeOperarVentanilla()).isFalse();

        usuario.otorgarPermiso(PermisoOperativo.REGISTRAR_PRESTAMO);

        assertThat(usuario.puedeOperarVentanilla()).isTrue();
    }

    @Test
    void noSeOtorganPermisosAProvisionales() {
        var usuario = Fixtures.alumnoProvisional();

        assertThatThrownBy(() -> usuario.otorgarPermiso(PermisoOperativo.REGISTRAR_PRESTAMO))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("USUARIO_NO_VALIDADO"));
    }

    @Test
    void losPermisosExpuestosSonInmutables() {
        var usuario = Fixtures.bibliotecario();

        assertThatThrownBy(() -> usuario.permisos().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
