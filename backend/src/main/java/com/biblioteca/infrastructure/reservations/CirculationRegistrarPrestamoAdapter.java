package com.biblioteca.infrastructure.reservations;

import com.biblioteca.circulation.application.command.RegistrarPrestamoCommand;
import com.biblioteca.circulation.application.usecase.RegistrarPrestamoUseCase;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.application.port.RegistrarPrestamoPort;
import com.biblioteca.shared.domain.BusinessRuleException;
import org.springframework.stereotype.Component;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.identity.domain.vo.UsuarioId;

import java.util.Objects;

@Component
public class CirculationRegistrarPrestamoAdapter implements RegistrarPrestamoPort {

    private final RegistrarPrestamoUseCase registrarPrestamo;
    private final EjemplarRepository ejemplares;

    public CirculationRegistrarPrestamoAdapter(RegistrarPrestamoUseCase registrarPrestamo,
                                             EjemplarRepository ejemplares) {
        this.registrarPrestamo = registrarPrestamo;
        this.ejemplares = ejemplares;
    }

    @Override
    public PrestamoId registrarPrestamo(UsuarioId operadorId, UsuarioId prestatarioId,
                                       CatalogoItemId catalogoItemId, com.biblioteca.inventory.domain.vo.EjemplarId ejemplarId) {
        Objects.requireNonNull(operadorId);
        Objects.requireNonNull(prestatarioId);
        Objects.requireNonNull(catalogoItemId);

        Ejemplar disponible = ejemplares.listarPorItem(catalogoItemId).stream()
                .filter(ejemplar -> ejemplarId == null || ejemplar.id().equals(ejemplarId))
                .filter(Ejemplar::estaDisponible)
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("EJEMPLAR_NO_DISPONIBLE",
                        "El ejemplar elegido no pertenece al bien o no esta disponible. Item " + catalogoItemId.value()));

        Prestamo prestamo = registrarPrestamo.ejecutar(new RegistrarPrestamoCommand(
                operadorId, prestatarioId, disponible.id(), LugarUso.EN_CAMPUS));
        return prestamo.id();
    }
}
