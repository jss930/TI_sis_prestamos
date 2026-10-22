package com.biblioteca.reservations.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.reservations.application.command.AceptarReservaCommand;
import com.biblioteca.reservations.application.port.RegistrarPrestamoPort;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class AceptarReservaUseCase {

    private final UsuarioRepository usuarios;
    private final CatalogoItemRepository catalogo;
    private final ReservaRepository reservas;
    private final RegistrarPrestamoPort prestamos;
    private final DomainEventPublisher eventos;

    @Autowired
    public AceptarReservaUseCase(UsuarioRepository usuarios,
                                 CatalogoItemRepository catalogo,
                                 ReservaRepository reservas,
                                 RegistrarPrestamoPort prestamos,
                                 DomainEventPublisher eventos) {
        this.usuarios = usuarios;
        this.catalogo = catalogo;
        this.reservas = reservas;
        this.prestamos = prestamos;
        this.eventos = eventos;
    }

    public AceptarReservaUseCase(UsuarioRepository usuarios,
                                 ReservaRepository reservas,
                                 CatalogoItemRepository catalogo,
                                 RegistrarPrestamoPort prestamos,
                                 DomainEventPublisher eventos) {
        this(usuarios, catalogo, reservas, prestamos, eventos);
    }

    public AceptarReservaUseCase(ReservaRepository reservas,
                                 UsuarioRepository usuarios,
                                 CatalogoItemRepository catalogo,
                                 RegistrarPrestamoPort prestamos,
                                 DomainEventPublisher eventos) {
        this(usuarios, catalogo, reservas, prestamos, eventos);
    }

    public Reserva ejecutar(AceptarReservaCommand command) {
        Objects.requireNonNull(command, "El comando es obligatorio");

        Usuario operador = usuarios.buscarPorId(command.operadorId())
                .orElseThrow(() -> new NotFoundException("Operador", command.operadorId().value()));
        if (!operador.puedeGestionarReservas() && !operador.puedeGestionarCatalogo()) {
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO",
                    "El operador no esta validado o no tiene el permiso GESTIONAR_RESERVAS");
        }

        Reserva reserva = reservas.buscarPorId(command.reservaId())
                .orElseThrow(() -> new NotFoundException("Reserva", command.reservaId().value()));

        if (reserva.estado() != com.biblioteca.reservations.domain.model.EstadoReserva.SOLICITADA) {
            throw new BusinessRuleException("TRANSICION_INVALIDA",
                    "Solo se aceptan reservas solicitadas");
        }

        Usuario prestatario = usuarios.buscarPorId(reserva.prestatarioId())
                .orElseThrow(() -> new NotFoundException("Prestatario", reserva.prestatarioId().value()));
        if (!prestatario.puedeSolicitarPrestamo()) {
            throw new BusinessRuleException("USUARIO_NO_HABILITADO",
                    "La cuenta " + prestatario.estado() + " no puede recibir el prestamo");
        }

        CatalogoItem item = catalogo.buscarPorId(reserva.catalogoItemId())
                .orElseThrow(() -> new NotFoundException("Item de catalogo", reserva.catalogoItemId().value()));
        if (!item.activo()) {
            throw new BusinessRuleException("ITEM_INACTIVO", "El item esta fuera de circulacion");
        }

        com.biblioteca.circulation.domain.vo.PrestamoId prestamoId =
                prestamos.registrarPrestamo(operador.id(), prestatario.id(), item.id(), command.ejemplarId());

        reserva.aceptar(operador.id(), prestamoId);
        reservas.guardar(reserva);
        eventos.publicarTodos(reserva.extraerEventos());
        return reserva;
    }
}
