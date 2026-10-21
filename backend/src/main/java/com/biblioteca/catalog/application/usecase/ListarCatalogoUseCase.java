package com.biblioteca.catalog.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ListarCatalogoUseCase {

    private final CatalogoItemRepository repositorio;

    public ListarCatalogoUseCase(CatalogoItemRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<CatalogoItem> ejecutar(String texto, TipoItem tipo) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return repositorio.listarTodos().stream()
                .filter(item -> tipo == null || item.tipo() == tipo)
                .filter(item -> filtro.isEmpty()
                        || item.titulo().value().toLowerCase(Locale.ROOT).contains(filtro))
                .toList();
    }
}
