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

    public RenovarPrestamoUseCase(PrestamoRepository prestamos,
                                  UsuarioRepository usuarios,
                                  PoliticaPrestamo politica,
                                  DomainEventPublisher eventos,
                                  Clock clock) {
        this.prestamos = prestamos;
        this.usuarios = usuarios;
        this.politica = politica;
        this.eventos = eventos;
        this.clock = clock;
    }

    public Prestamo ejecutar(RenovarPrestamoCommand command) {
        Prestamo prestamo = prestamos.buscarPorId(command.prestamoId())
                .orElseThrow(() -> new NotFoundException("Prestamo", command.prestamoId().value()));
        Usuario prestatario = usuarios.buscarPorId(prestamo.prestatarioId())
                .orElseThrow(() -> new NotFoundException("Prestatario", prestamo.prestatarioId().value()));

        if (prestamo.renovaciones() >= politica.maxRenovaciones(prestatario.perfil())) {
            throw new BusinessRuleException("LIMITE_RENOVACIONES",
                    "El prestamo alcanzo el maximo de renovaciones permitidas");
        }

        LocalDate hoy = LocalDate.now(clock);
        prestamo.renovar(hoy, prestamo.periodo().diasTotales());
        prestamos.guardar(prestamo);
        eventos.publicarTodos(prestamo.extraerEventos());
        return prestamo;
    }
}
