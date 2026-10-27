package com.biblioteca.infrastructure.config;

import com.biblioteca.reservations.application.usecase.ExpirarSolicitudUseCase;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;
import java.time.Clock;

@Configuration
@EnableScheduling
public class ExpiracionSolicitudesConfig {
    private final ReservaRepository reservas;
    private final ExpirarSolicitudUseCase expirar;
    private final Clock clock;
    public ExpiracionSolicitudesConfig(ReservaRepository reservas, ExpirarSolicitudUseCase expirar, Clock clock) {
        this.reservas=reservas; this.expirar=expirar; this.clock=clock;
    }
    @Scheduled(fixedDelay=30000, initialDelay=30000)
    public void liberarVencidas() {
        for (var reserva : reservas.listarPorEstado(EstadoReserva.APROBADA))
            if (!clock.instant().isBefore(reserva.fechaLimiteRetiro())) expirar.ejecutar(reserva.id());
    }
}
