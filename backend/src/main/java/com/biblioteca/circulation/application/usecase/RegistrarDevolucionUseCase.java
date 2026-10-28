package com.biblioteca.circulation.application.usecase;

import com.biblioteca.circulation.application.command.RegistrarDevolucionCommand;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

@Service
@Transactional
public class RegistrarDevolucionUseCase {

    private final PrestamoRepository prestamos;
    private final EjemplarRepository ejemplares;
    private final DomainEventPublisher eventos;
    private final Clock clock;

    public RegistrarDevolucionUseCase(PrestamoRepository prestamos,
                                      EjemplarRepository ejemplares,
                                      DomainEventPublisher eventos,
                                      Clock clock) {
        this.prestamos = prestamos;
        this.ejemplares = ejemplares;
        this.eventos = eventos;
        this.clock = clock;
    }

    public Prestamo ejecutar(RegistrarDevolucionCommand command) {
        Objects.requireNonNull(command.estadoFisico(), "El estado fisico es obligatorio");
        Prestamo prestamo = prestamos.buscarPorId(command.prestamoId())
                .orElseThrow(() -> new NotFoundException("Prestamo", command.prestamoId().value()));
        Ejemplar ejemplar = ejemplares.buscarPorId(prestamo.ejemplarId())
                .orElseThrow(() -> new NotFoundException("Ejemplar", prestamo.ejemplarId().value()));

        LocalDate hoy = LocalDate.now(clock);
        prestamo.registrarDevolucion(hoy, clock.instant(), command.estadoFisico());
        ejemplar.devolver(command.estadoFisico());

        prestamos.guardar(prestamo);
        ejemplares.guardar(ejemplar);

        eventos.publicarTodos(prestamo.extraerEventos());
        eventos.publicarTodos(ejemplar.extraerEventos());
        return prestamo;
    }
}
