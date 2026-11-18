package com.biblioteca.infrastructure.web;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.application.command.*;
import com.biblioteca.circulation.application.usecase.*;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.application.command.SolicitarReservaCommand;
import com.biblioteca.reservations.application.usecase.*;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.support.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlazosReservasTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired CatalogoItemRepository catalogo;
    @Autowired EjemplarRepository ejemplares;
    @Autowired SolicitarReservaUseCase solicitar;
    @Autowired ReprogramarReservaUseCase reprogramar;
    @Autowired RegistrarPrestamoUseCase prestar;
    @Autowired RenovarPrestamoUseCase renovar;
    @MockBean Clock clock;
    Usuario alumno;
    Instant ahora = Instant.parse("2026-11-17T12:00:00Z");

    @BeforeEach
    void preparar() {
        when(clock.instant()).thenReturn(ahora);
        when(clock.getZone()).thenReturn(ZoneId.of("America/Lima"));
        alumno = usuarios.guardar(Fixtures.alumnoValidado());
    }

    @Test
    void solicitudYReprogramacionRespetanElPerfil() {
        var libro = catalogo.guardar(Fixtures.libro());
        ejemplares.guardar(Fixtures.ejemplarDisponible(libro.id()));
        var inicio = ahora.plus(Duration.ofDays(1));
        assertThatThrownBy(() -> solicitar.ejecutar(new SolicitarReservaCommand(
                alumno.id(), libro.id(), LugarUso.DOMICILIO, inicio, inicio.plus(Duration.ofDays(4)))))
                .isInstanceOf(BusinessRuleException.class);
        var reserva = solicitar.ejecutar(new SolicitarReservaCommand(
                alumno.id(), libro.id(), LugarUso.DOMICILIO, inicio, inicio.plus(Duration.ofDays(3))));
        assertThatThrownBy(() -> reprogramar.ejecutar(alumno.id(), reserva.id(),
                inicio, inicio.plus(Duration.ofDays(4)))).isInstanceOf(BusinessRuleException.class);
        assertThat(reprogramar.ejecutar(alumno.id(), reserva.id(), inicio,
                inicio.plus(Duration.ofHours(1))).finReserva()).isEqualTo(inicio.plus(Duration.ofHours(1)));
    }

    @Test
    void docentePuedeReservarLibroPorCincoDias() {
        var docente = usuarios.guardar(Fixtures.docenteValidado());
        var libro = catalogo.guardar(Fixtures.libro());
        ejemplares.guardar(Fixtures.ejemplarDisponible(libro.id()));
        var inicio = ahora.plus(Duration.ofDays(1));
        assertThat(solicitar.ejecutar(new SolicitarReservaCommand(docente.id(), libro.id(),
                LugarUso.DOMICILIO, inicio, inicio.plus(Duration.ofDays(5)))).finReserva())
                .isEqualTo(inicio.plus(Duration.ofDays(5)));
    }

    @Test
    void renovacionNoPuedeEvitarElMaximoTotal() {
        var operador = usuarios.guardar(Fixtures.bibliotecario());
        var libro = catalogo.guardar(Fixtures.libro());
        var copia = ejemplares.guardar(Fixtures.ejemplarDisponible(libro.id()));
        var prestamo = prestar.ejecutar(new RegistrarPrestamoCommand(operador.id(), alumno.id(),
                copia.id(), LugarUso.EN_CAMPUS, ahora, ahora.plus(Duration.ofHours(1))));
        assertThatThrownBy(() -> renovar.ejecutar(new RenovarPrestamoCommand(prestamo.id(),
                ahora.plus(Duration.ofDays(1))))).isInstanceOf(BusinessRuleException.class);
        assertThat(renovar.ejecutar(new RenovarPrestamoCommand(prestamo.id(),
                ahora.plus(Duration.ofHours(2)))).fechaHoraVencimiento())
                .isEqualTo(ahora.plus(Duration.ofHours(2)));
    }
}
