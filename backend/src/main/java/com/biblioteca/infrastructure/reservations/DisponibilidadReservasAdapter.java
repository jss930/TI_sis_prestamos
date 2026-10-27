package com.biblioteca.infrastructure.reservations;

import com.biblioteca.circulation.application.port.DisponibilidadReservasPort;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.policy.PoliticaHorarioReserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DisponibilidadReservasAdapter implements DisponibilidadReservasPort {
    private final ReservaRepository reservas;

    public DisponibilidadReservasAdapter(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    @Override
    public boolean existeConflicto(EjemplarId ejemplarId, UsuarioId prestatarioId,
                                   Instant inicio, Instant fin) {
        return reservas.listarTodos().stream().anyMatch(r -> r.estado() == EstadoReserva.APROBADA
                && ejemplarId.equals(r.ejemplarId())
                && r.inicioReserva() != null && r.finReserva() != null
                // El titular puede recoger su propia reserva durante la hora de tolerancia.
                && !(r.prestatarioId().equals(prestatarioId)
                    && !inicio.isBefore(r.inicioReserva())
                    && inicio.isBefore(r.fechaLimiteRetiro()))
                && PoliticaHorarioReserva.seSolapan(inicio, fin, r.inicioReserva(), r.finReserva()));
    }
}
