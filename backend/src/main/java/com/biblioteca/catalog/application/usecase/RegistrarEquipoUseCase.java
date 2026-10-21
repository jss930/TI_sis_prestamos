package com.biblioteca.catalog.application.usecase;

import com.biblioteca.catalog.application.command.RegistrarEquipoCommand;
import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class RegistrarEquipoUseCase {

    private final CatalogoItemRepository repositorio;
    private final DomainEventPublisher eventos;

    public RegistrarEquipoUseCase(CatalogoItemRepository repositorio, DomainEventPublisher eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    public CatalogoItem ejecutar(RegistrarEquipoCommand command) {
        CatalogoItem item = CatalogoItem.registrarEquipo(
                command.titulo(), command.marca(), command.modelo());
        CatalogoItem guardado = repositorio.guardar(item);
        eventos.publicarTodos(item.extraerEventos());
        return guardado;
    }
}
