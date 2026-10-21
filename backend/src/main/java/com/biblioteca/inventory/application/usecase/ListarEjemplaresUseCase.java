package com.biblioteca.inventory.application.usecase;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarEjemplaresUseCase {

    private final EjemplarRepository ejemplares;

    public ListarEjemplaresUseCase(EjemplarRepository ejemplares) {
        this.ejemplares = ejemplares;
    }

    public List<Ejemplar> ejecutar(CatalogoItemId catalogoItemId) {
        return catalogoItemId == null
                ? ejemplares.listarTodos()
                : ejemplares.listarPorItem(catalogoItemId);
    }
}
