package com.biblioteca.inventory.domain.repository;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.inventory.domain.vo.EjemplarId;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del contexto inventory. */
public interface EjemplarRepository {

    Ejemplar guardar(Ejemplar ejemplar);

    Optional<Ejemplar> buscarPorId(EjemplarId id);

    Optional<Ejemplar> buscarPorBarcode(Barcode barcode);

    List<Ejemplar> listarTodos();

    List<Ejemplar> listarPorItem(CatalogoItemId catalogoItemId);

    boolean existePorBarcode(Barcode barcode);
}
