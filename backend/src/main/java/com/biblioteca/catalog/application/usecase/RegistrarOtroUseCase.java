package com.biblioteca.catalog.application.usecase;

import com.biblioteca.catalog.application.command.RegistrarOtroCommand;
import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class RegistrarOtroUseCase {

    private final CatalogoItemRepository repositorio;
    private final DomainEventPublisher eventos;

    public RegistrarOtroUseCase(CatalogoItemRepository repositorio, DomainEventPublisher eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    public CatalogoItem ejecutar(RegistrarOtroCommand command) {
        CatalogoItem item = CatalogoItem.registrarOtro(command.titulo(), command.descripcion());
        CatalogoItem guardado = repositorio.guardar(item);
        eventos.publicarTodos(item.extraerEventos());
        return guardado;
    }
}
