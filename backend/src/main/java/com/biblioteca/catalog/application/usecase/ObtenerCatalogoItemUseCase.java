package com.biblioteca.catalog.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ObtenerCatalogoItemUseCase {

    private final CatalogoItemRepository repositorio;

    public ObtenerCatalogoItemUseCase(CatalogoItemRepository repositorio) {
        this.repositorio = repositorio;
    }

    public CatalogoItem ejecutar(CatalogoItemId id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Item de catalogo", id.value()));
    }
}
