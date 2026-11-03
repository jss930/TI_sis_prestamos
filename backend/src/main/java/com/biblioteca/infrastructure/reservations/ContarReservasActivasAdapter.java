package com.biblioteca.infrastructure.reservations;

import com.biblioteca.circulation.application.port.ContarReservasActivasPort;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import org.springframework.stereotype.Component;

@Component
public class ContarReservasActivasAdapter implements ContarReservasActivasPort {
    private final ReservaRepository reservas;

    public ContarReservasActivasAdapter(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    @Override
    public long contar(UsuarioId prestatarioId) {
        return reservas.contarActivasPor(prestatarioId);
    }
}
