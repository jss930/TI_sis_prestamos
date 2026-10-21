package com.biblioteca.catalog.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActivarCatalogoItemUseCase {

    private final CatalogoItemRepository repositorio;

    public ActivarCatalogoItemUseCase(CatalogoItemRepository repositorio) {
        this.repositorio = repositorio;
    }

    public CatalogoItem ejecutar(CatalogoItemId id) {
        CatalogoItem item = repositorio.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Item de catalogo", id.value()));
        item.activar();
        return repositorio.guardar(item);
    }
}