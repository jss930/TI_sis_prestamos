package com.biblioteca.catalog.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.Titulo;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActualizarTituloCatalogoItemUseCase {

    private final CatalogoItemRepository repositorio;

    public ActualizarTituloCatalogoItemUseCase(CatalogoItemRepository repositorio) {
        this.repositorio = repositorio;
    }

    public CatalogoItem ejecutar(CatalogoItemId id, Titulo nuevoTitulo) {
        CatalogoItem item = repositorio.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Item de catalogo", id.value()));
        item.actualizarTitulo(nuevoTitulo);
        return repositorio.guardar(item);
    }
}