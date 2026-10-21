package com.biblioteca.catalog.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.infrastructure.persistence.inmemory.InMemoryCatalogoItemRepository;
import com.biblioteca.shared.domain.NotFoundException;
import com.biblioteca.support.Fixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivarCatalogoItemUseCaseTest {

    @Test
    void activaUnItemDesactivado() {
        CatalogoItemRepository repositorio = new InMemoryCatalogoItemRepository();
        CatalogoItem item = Fixtures.libro();
        item.desactivar();
        repositorio.guardar(item);

        ActivarCatalogoItemUseCase useCase = new ActivarCatalogoItemUseCase(repositorio);

        CatalogoItem activado = useCase.ejecutar(item.id());

        assertThat(activado.activo()).isTrue();
    }

    @Test
    void esIdempotenteSiYaEstaActivo() {
        CatalogoItemRepository repositorio = new InMemoryCatalogoItemRepository();
        CatalogoItem item = Fixtures.libro();
        repositorio.guardar(item);

        ActivarCatalogoItemUseCase useCase = new ActivarCatalogoItemUseCase(repositorio);

        CatalogoItem resultado = useCase.ejecutar(item.id());

        assertThat(resultado.activo()).isTrue();
    }

    @Test
    void fallaSiElItemNoExiste() {
        CatalogoItemRepository repositorio = new InMemoryCatalogoItemRepository();
        ActivarCatalogoItemUseCase useCase = new ActivarCatalogoItemUseCase(repositorio);

        assertThatThrownBy(() -> useCase.ejecutar(new CatalogoItemId(java.util.UUID.randomUUID())))
                .isInstanceOf(NotFoundException.class);
    }
}