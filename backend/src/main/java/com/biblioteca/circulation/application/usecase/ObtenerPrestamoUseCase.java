package com.biblioteca.circulation.application.usecase;

import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ObtenerPrestamoUseCase {

    private final PrestamoRepository prestamos;

    public ObtenerPrestamoUseCase(PrestamoRepository prestamos) {
        this.prestamos = prestamos;
    }

    public Prestamo ejecutar(PrestamoId id) {
        return prestamos.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Prestamo", id.value()));
    }
}
