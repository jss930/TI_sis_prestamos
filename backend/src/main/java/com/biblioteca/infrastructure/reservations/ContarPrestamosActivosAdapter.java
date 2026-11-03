package com.biblioteca.infrastructure.reservations;

import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.application.port.ContarPrestamosActivosPort;
import org.springframework.stereotype.Component;

@Component
public class ContarPrestamosActivosAdapter implements ContarPrestamosActivosPort {
    private final PrestamoRepository prestamos;

    public ContarPrestamosActivosAdapter(PrestamoRepository prestamos) {
        this.prestamos = prestamos;
    }

    @Override
    public long contar(UsuarioId prestatarioId) {
        return prestamos.contarActivosPor(prestatarioId);
    }

    @Override
    public boolean tieneVencido(UsuarioId prestatarioId, java.time.LocalDate fecha) {
        return prestamos.tieneVencido(prestatarioId, fecha);
    }
}
