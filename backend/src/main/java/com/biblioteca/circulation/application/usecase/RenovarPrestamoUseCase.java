package com.biblioteca.circulation.application.usecase;

import com.biblioteca.circulation.application.command.RenovarPrestamoCommand;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.policy.PoliticaPrestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@Transactional
public class RenovarPrestamoUseCase {

    private final PrestamoRepository prestamos;
    private final UsuarioRepository usuarios;
    private final PoliticaPrestamo politica;
    private final DomainEventPublisher eventos;
    private final Clock clock;
    private final com.biblioteca.catalog.domain.repository.CatalogoItemRepository catalogo;
    private final com.biblioteca.inventory.domain.repository.EjemplarRepository ejemplares;
    private final com.biblioteca.circulation.application.port.DisponibilidadReservasPort disponibilidad;

    public RenovarPrestamoUseCase(PrestamoRepository prestamos,
                                  UsuarioRepository usuarios,
                                  PoliticaPrestamo politica,
                                  DomainEventPublisher eventos,
                                  Clock clock,
                                  com.biblioteca.catalog.domain.repository.CatalogoItemRepository catalogo,
                                  com.biblioteca.inventory.domain.repository.EjemplarRepository ejemplares,
                                  com.biblioteca.circulation.application.port.DisponibilidadReservasPort disponibilidad) {
        this.prestamos = prestamos;
        this.usuarios = usuarios;
        this.politica = politica;
        this.eventos = eventos;
        this.clock = clock;
        this.catalogo = catalogo;
        this.ejemplares = ejemplares;
        this.disponibilidad = disponibilidad;
    }

    public Prestamo ejecutar(RenovarPrestamoCommand command) {
        Prestamo prestamo = prestamos.buscarPorIdParaActualizar(command.prestamoId())
                .orElseThrow(() -> new NotFoundException("Prestamo", command.prestamoId().value()));
        Usuario prestatario = usuarios.buscarPorId(prestamo.prestatarioId())
                .orElseThrow(() -> new NotFoundException("Prestatario", prestamo.prestatarioId().value()));

        if (prestamo.renovaciones() >= politica.maxRenovaciones(prestatario.perfil())) {
            throw new BusinessRuleException("LIMITE_RENOVACIONES",
                    "El prestamo alcanzo el maximo de renovaciones permitidas");
        }

        var ejemplar = ejemplares.buscarPorIdParaActualizar(prestamo.ejemplarId())
                .orElseThrow(() -> new NotFoundException("Ejemplar", prestamo.ejemplarId().value()));
        var item = catalogo.buscarPorId(ejemplar.catalogoItemId())
                .orElseThrow(() -> new NotFoundException("Item", ejemplar.catalogoItemId().value()));
        var fin = command.nuevoVencimiento() == null
                ? prestamo.fechaHoraVencimiento().plus(java.time.Duration.ofHours(1))
                : command.nuevoVencimiento();
        com.biblioteca.circulation.domain.policy.PoliticaPlazoPrestamo.validar(
                prestamo.fechaHoraEntrega(), fin, prestamo.lugarUso(), prestatario.perfil(), item.tipo(), politica);
        if (disponibilidad.existeConflicto(ejemplar.id(), prestatario.id(), prestamo.fechaHoraVencimiento(), fin))
            throw new BusinessRuleException("EJEMPLAR_RESERVADO_EN_HORARIO", "La renovacion cruza una reserva");
        prestamo.renovarHasta(clock.instant(), fin,
                com.biblioteca.circulation.domain.policy.PoliticaPlazoPrestamo.ZONA);
        prestamos.guardar(prestamo);
        eventos.publicarTodos(prestamo.extraerEventos());
        return prestamo;
    }
}
