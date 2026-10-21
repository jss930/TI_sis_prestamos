package com.biblioteca.inventory.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.inventory.application.command.RegistrarEjemplarCommand;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrarEjemplarUseCase {

    private final CatalogoItemRepository catalogo;
    private final EjemplarRepository ejemplares;
    private final DomainEventPublisher eventos;

    public RegistrarEjemplarUseCase(CatalogoItemRepository catalogo,
                                    EjemplarRepository ejemplares,
                                    DomainEventPublisher eventos) {
        this.catalogo = catalogo;
        this.ejemplares = ejemplares;
        this.eventos = eventos;
    }

    public Ejemplar ejecutar(RegistrarEjemplarCommand command) {
        CatalogoItem item = catalogo.buscarPorId(command.catalogoItemId())
                .orElseThrow(() -> new NotFoundException("Item de catalogo", command.catalogoItemId().value()));
        if (!item.activo()) {
            throw new BusinessRuleException("ITEM_INACTIVO",
                    "No se pueden registrar ejemplares de un item inactivo");
        }
        if (ejemplares.existePorBarcode(command.barcode())) {
            throw new BusinessRuleException("BARCODE_DUPLICADO",
                    "Ya existe un ejemplar con el codigo " + command.barcode().value());
        }
        Ejemplar ejemplar = Ejemplar.registrar(
                command.catalogoItemId(), command.barcode(), command.ubicacion(), command.notas());
        Ejemplar guardado = ejemplares.guardar(ejemplar);
        eventos.publicarTodos(ejemplar.extraerEventos());
        return guardado;
    }
}
