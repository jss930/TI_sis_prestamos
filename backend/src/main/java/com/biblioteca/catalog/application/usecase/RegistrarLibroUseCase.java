package com.biblioteca.catalog.application.usecase;

import com.biblioteca.catalog.application.command.RegistrarLibroCommand;
import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import org.springframework.stereotype.Service;

@Service
public class RegistrarLibroUseCase {

    private final CatalogoItemRepository repositorio;
    private final DomainEventPublisher eventos;

    public RegistrarLibroUseCase(CatalogoItemRepository repositorio, DomainEventPublisher eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    public CatalogoItem ejecutar(RegistrarLibroCommand command) {
        if (repositorio.existePorIsbn(command.isbn())) {
            throw new BusinessRuleException("ISBN_DUPLICADO",
                    "Ya existe un libro con el ISBN " + command.isbn().value());
        }
        CatalogoItem item = CatalogoItem.registrarLibro(
                command.titulo(), command.isbn(), command.autor(),
                command.editorial(), command.anioPublicacion());
        CatalogoItem guardado = repositorio.guardar(item);
        eventos.publicarTodos(item.extraerEventos());
        return guardado;
    }
}
