package com.biblioteca.catalog.domain.repository;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.ISBN;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del contexto catalog. */
public interface CatalogoItemRepository {

    CatalogoItem guardar(CatalogoItem item);

    Optional<CatalogoItem> buscarPorId(CatalogoItemId id);

    List<CatalogoItem> listarTodos();

    boolean existePorIsbn(ISBN isbn);
}
