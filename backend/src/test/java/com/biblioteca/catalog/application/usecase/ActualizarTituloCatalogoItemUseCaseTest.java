package com.biblioteca.catalog.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.Titulo;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryCatalogoItemRepository;
import com.biblioteca.shared.domain.NotFoundException;
import com.biblioteca.support.Fixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActualizarTituloCatalogoItemUseCaseTest {

    @Test
    void actualizaElTituloDelItem() {
        CatalogoItemRepository repositorio = new InMemoryCatalogoItemRepository();
        CatalogoItem item = Fixtures.libro();
        repositorio.guardar(item);

        ActualizarTituloCatalogoItemUseCase useCase = new ActualizarTituloCatalogoItemUseCase(repositorio);

        CatalogoItem actualizado = useCase.ejecutar(item.id(), new Titulo("Clean Code (edicion revisada)"));

        assertThat(actualizado.titulo().value()).isEqualTo("Clean Code (edicion revisada)");
        assertThat(repositorio.buscarPorId(item.id()).orElseThrow().titulo().value())
                .isEqualTo("Clean Code (edicion revisada)");
    }

    @Test
    void fallaSiElItemNoExiste() {
        CatalogoItemRepository repositorio = new InMemoryCatalogoItemRepository();
        ActualizarTituloCatalogoItemUseCase useCase = new ActualizarTituloCatalogoItemUseCase(repositorio);

        assertThatThrownBy(() -> useCase.ejecutar(new CatalogoItemId(java.util.UUID.randomUUID()),
                new Titulo("Nuevo titulo")))
                .isInstanceOf(NotFoundException.class);
    }
}