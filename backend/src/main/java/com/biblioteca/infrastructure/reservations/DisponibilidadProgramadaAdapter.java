package com.biblioteca.infrastructure.reservations;

import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.reservations.application.port.DisponibilidadProgramadaPort;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DisponibilidadProgramadaAdapter implements DisponibilidadProgramadaPort {
    private final PrestamoRepository prestamos;
    private final ReservaRepository reservas;

    public DisponibilidadProgramadaAdapter(PrestamoRepository prestamos, ReservaRepository reservas) {
        this.prestamos = prestamos;
        this.reservas = reservas;
    }

    @Override
    public boolean estaLibre(EjemplarId ejemplarId, Instant inicio, Instant fin, ReservaId excluir) {
        return !prestamos.existeSolapamiento(ejemplarId, inicio, fin)
                && !reservas.existeSolapamiento(ejemplarId, inicio, fin, excluir);
    }
}
