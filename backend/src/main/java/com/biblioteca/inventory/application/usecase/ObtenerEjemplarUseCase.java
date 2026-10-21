package com.biblioteca.inventory.application.usecase;

import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ObtenerEjemplarUseCase {

    private final EjemplarRepository ejemplares;

    public ObtenerEjemplarUseCase(EjemplarRepository ejemplares) {
        this.ejemplares = ejemplares;
    }

    public Ejemplar porId(EjemplarId id) {
        return ejemplares.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Ejemplar", id.value()));
    }

    public Ejemplar porBarcode(Barcode barcode) {
        return ejemplares.buscarPorBarcode(barcode)
                .orElseThrow(() -> new NotFoundException("Ejemplar", barcode.value()));
    }
}
