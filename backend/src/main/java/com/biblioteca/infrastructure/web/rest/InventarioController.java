package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.inventory.application.command.RegistrarEjemplarCommand;
import com.biblioteca.inventory.application.usecase.DarDeBajaEjemplarUseCase;
import com.biblioteca.inventory.application.usecase.ListarEjemplaresUseCase;
import com.biblioteca.inventory.application.usecase.ObtenerEjemplarUseCase;
import com.biblioteca.inventory.application.usecase.RegistrarEjemplarUseCase;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.model.Ubicacion;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventario/ejemplares")
public class InventarioController {

    private final RegistrarEjemplarUseCase registrar;
    private final ListarEjemplaresUseCase listar;
    private final ObtenerEjemplarUseCase obtener;
    private final DarDeBajaEjemplarUseCase darDeBaja;

    public InventarioController(RegistrarEjemplarUseCase registrar,
                                ListarEjemplaresUseCase listar,
                                ObtenerEjemplarUseCase obtener,
                                DarDeBajaEjemplarUseCase darDeBaja) {
        this.registrar = registrar;
        this.listar = listar;
        this.obtener = obtener;
        this.darDeBaja = darDeBaja;
    }

    @GetMapping
    public List<EjemplarResponse> listar(@RequestParam(required = false) UUID catalogoItemId,
                                         @RequestParam(required = false) String barcode) {
        if (barcode != null && !barcode.isBlank()) {
            return List.of(aResponse(obtener.porBarcode(new Barcode(barcode))));
        }
        CatalogoItemId itemId = catalogoItemId == null ? null : new CatalogoItemId(catalogoItemId);
        return listar.ejecutar(itemId).stream().map(InventarioController::aResponse).toList();
    }

    @GetMapping("/{id}")
    public EjemplarResponse obtener(@PathVariable UUID id) {
        return aResponse(obtener.porId(new EjemplarId(id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EjemplarResponse registrar(@Valid @RequestBody RegistrarEjemplarRequest request) {
        Ejemplar ejemplar = registrar.ejecutar(new RegistrarEjemplarCommand(
                new CatalogoItemId(request.catalogoItemId()),
                new Barcode(request.barcode()),
                new Ubicacion(request.sala(), request.estante()),
                request.notas()));
        return aResponse(ejemplar);
    }

    @PostMapping("/{id}/baja")
    public EjemplarResponse darDeBaja(@PathVariable UUID id, @Valid @RequestBody BajaRequest request) {
        return aResponse(darDeBaja.ejecutar(new EjemplarId(id), request.motivo()));
    }

    private static EjemplarResponse aResponse(Ejemplar ejemplar) {
        return new EjemplarResponse(ejemplar.id().value(), ejemplar.catalogoItemId().value(),
                ejemplar.barcode().value(), ejemplar.estadoFisico(),
                ejemplar.ubicacion().sala(), ejemplar.ubicacion().estante(), ejemplar.notas());
    }

    public record RegistrarEjemplarRequest(
            @NotNull UUID catalogoItemId,
            @NotBlank String barcode,
            String sala,
            String estante,
            String notas) {
    }

    public record BajaRequest(@NotBlank String motivo) {
    }

    public record EjemplarResponse(
            UUID id,
            UUID catalogoItemId,
            String barcode,
            EstadoFisico estadoFisico,
            String sala,
            String estante,
            String notas) {
    }
}
