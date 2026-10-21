package com.biblioteca.inventory.application.usecase;

import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DarDeBajaEjemplarUseCase {

    private final EjemplarRepository ejemplares;

    public DarDeBajaEjemplarUseCase(EjemplarRepository ejemplares) {
        this.ejemplares = ejemplares;
    }

    public Ejemplar ejecutar(EjemplarId id, String motivo) {
        Ejemplar ejemplar = ejemplares.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Ejemplar", id.value()));
        ejemplar.darDeBaja(motivo);
        return ejemplares.guardar(ejemplar);
    }
}
