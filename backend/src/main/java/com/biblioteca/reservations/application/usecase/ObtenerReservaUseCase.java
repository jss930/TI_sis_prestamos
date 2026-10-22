package com.biblioteca.reservations.application.usecase;

import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.domain.NotFoundException;

@org.springframework.stereotype.Service
public class ObtenerReservaUseCase {

    private final ReservaRepository reservas;

    public ObtenerReservaUseCase(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    public Reserva ejecutar(ReservaId id) {
        return reservas.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Reserva", id.value()));
    }
}
