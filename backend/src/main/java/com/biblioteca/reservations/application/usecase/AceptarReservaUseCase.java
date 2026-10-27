package com.biblioteca.reservations.application.usecase;

import com.biblioteca.shared.application.port.DomainEventPublisher;

import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.policy.PoliticaPrestamo;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.reservations.application.command.AceptarReservaCommand;
import com.biblioteca.reservations.domain.model.*;
import com.biblioteca.reservations.domain.policy.PoliticaElegibilidadSolicitud;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.shared.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
@Transactional
public class AceptarReservaUseCase {
    private final UsuarioRepository usuarios;
    private final ReservaRepository reservas;
    private final EjemplarRepository ejemplares;
    private final PrestamoRepository prestamos;
    private final SancionRepository sanciones;
    private final PoliticaPrestamo politica;
    private final AccesoSolicitudes acceso;
    private final Clock clock;
    private final DomainEventPublisher eventos;
    public AceptarReservaUseCase(UsuarioRepository usuarios, ReservaRepository reservas, EjemplarRepository ejemplares,
            PrestamoRepository prestamos, SancionRepository sanciones, PoliticaPrestamo politica, AccesoSolicitudes acceso, Clock clock, DomainEventPublisher eventos) {
        this.usuarios=usuarios; this.reservas=reservas; this.ejemplares=ejemplares; this.prestamos=prestamos;
        this.sanciones=sanciones; this.politica=politica; this.acceso=acceso; this.clock=clock; this.eventos=eventos;
    }
    public Reserva ejecutar(AceptarReservaCommand command) {
        var reserva = reservas.buscarPorIdParaActualizar(command.reservaId()).orElseThrow(() -> new NotFoundException("Reserva", command.reservaId().value()));
        acceso.exigirResponsable(command.operadorId(), reserva);
        if (reserva.estado() != EstadoReserva.SOLICITADA) throw new BusinessRuleException("TRANSICION_INVALIDA", "Solo se aprueban solicitudes pendientes");
        var alumno = usuarios.buscarPorIdParaActualizar(reserva.prestatarioId()).orElseThrow(() -> new NotFoundException("Usuario", reserva.prestatarioId().value()));
        var item = acceso.item(reserva);
        var hoy = LocalDate.now(clock);
        long apartados = reservas.listarPorPrestatario(alumno.id()).stream().filter(r -> r.estado()==EstadoReserva.APROBADA).count();
        PoliticaElegibilidadSolicitud.validar(acceso.usuario(command.operadorId()), alumno, item, reserva.lugarUso(),
                sanciones.existeActivaPara(alumno.id(), hoy), prestamos.tieneVencido(alumno.id(), hoy), prestamos.contarActivosPor(alumno.id())+apartados, politica);
        var id = command.ejemplarId();
        if (id == null) id = ejemplares.listarPorItem(item.id()).stream().filter(e -> e.estaDisponible())
                .map(e -> e.id()).findFirst().orElseThrow(() -> new BusinessRuleException("EJEMPLAR_NO_DISPONIBLE", "No hay ejemplares disponibles"));
        var ejemplar = ejemplares.buscarPorIdParaActualizar(id).orElseThrow(() -> new BusinessRuleException("EJEMPLAR_NO_DISPONIBLE", "El ejemplar no existe"));
        if (!ejemplar.catalogoItemId().equals(item.id()) || prestamos.existeActivoPara(ejemplar.id()))
            throw new BusinessRuleException("EJEMPLAR_NO_DISPONIBLE", "El ejemplar no pertenece al bien o ya esta prestado");
        ejemplar.reservar();
        reserva.aprobar(command.operadorId(), ejemplar.id(), clock.instant(), clock.instant().plus(Duration.ofHours(48)));
        ejemplares.guardar(ejemplar);
        reservas.guardar(reserva);
        eventos.publicarTodos(reserva.extraerEventos());
        return reserva;
    }
}
