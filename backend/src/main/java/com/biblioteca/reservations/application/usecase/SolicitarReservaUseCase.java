package com.biblioteca.reservations.application.usecase;

import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.domain.model.EstadoReserva;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.reservations.application.command.SolicitarReservaCommand;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.policy.PoliticaReserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

@Service
@Transactional
public class SolicitarReservaUseCase {

    private final UsuarioRepository usuarios;
    private final CatalogoItemRepository catalogo;
    private final SancionRepository sanciones;
    private final ReservaRepository reservas;
    private final PoliticaReserva politica;
    private final DomainEventPublisher eventos;
    private final Clock clock;
    private final EjemplarRepository ejemplares;

    @Autowired
    public SolicitarReservaUseCase(UsuarioRepository usuarios,
                                   CatalogoItemRepository catalogo,
                                   SancionRepository sanciones,
                                   ReservaRepository reservas,
                                   PoliticaReserva politica,
                                   DomainEventPublisher eventos,
                                   Clock clock, EjemplarRepository ejemplares) {
        this.usuarios = usuarios;
        this.catalogo = catalogo;
        this.sanciones = sanciones;
        this.reservas = reservas;
        this.politica = politica;
        this.eventos = eventos;
        this.clock = clock;
        this.ejemplares = ejemplares;
    }

    public Reserva ejecutar(SolicitarReservaCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");
        LocalDate hoy = LocalDate.now(clock);

        Usuario prestatario = usuarios.buscarPorIdParaActualizar(command.prestatarioId())
                .orElseThrow(() -> new NotFoundException("Prestatario", command.prestatarioId().value()));
        if (!prestatario.puedeSolicitarPrestamo()) {
            throw new BusinessRuleException("USUARIO_NO_HABILITADO",
                    "La cuenta " + prestatario.estado() + " no puede solicitar reservas");
        }

        CatalogoItem item = catalogo.buscarPorId(command.catalogoItemId())
                .orElseThrow(() -> new NotFoundException("Item de catalogo", command.catalogoItemId().value()));
        if (!item.activo()) {
            throw new BusinessRuleException("ITEM_INACTIVO", "El item esta fuera de circulacion");
        }

        if (sanciones.existeActivaPara(prestatario.id(), hoy)) {
            throw new BusinessRuleException("SANCION_ACTIVA",
                    "El prestatario tiene una sancion activa");
        }

        if (ejemplares.listarPorItem(item.id()).stream().noneMatch(e -> e.estaDisponible()))
            throw new BusinessRuleException("EJEMPLAR_NO_DISPONIBLE", "No hay ejemplares disponibles para solicitar este bien");
        if (reservas.listarPorPrestatario(prestatario.id()).stream().anyMatch(r -> r.catalogoItemId().equals(item.id())
                && (r.estado() == EstadoReserva.SOLICITADA
                || r.estado() == EstadoReserva.APROBADA)))
            throw new BusinessRuleException("SOLICITUD_DUPLICADA", "Ya tienes una solicitud activa para este bien");
        long activas = reservas.contarActivasPor(prestatario.id());
        if (activas >= politica.maxReservasActivas(prestatario.perfil())) {
            throw new BusinessRuleException("LIMITE_RESERVAS",
                    "El prestatario alcanzo el maximo de reservas activas");
        }

        Reserva reserva = Reserva.solicitar(prestatario.id(), item.id(), command.lugarUso(), clock.instant());
        reservas.guardar(reserva);
        eventos.publicarTodos(reserva.extraerEventos());
        return reserva;
    }
}
