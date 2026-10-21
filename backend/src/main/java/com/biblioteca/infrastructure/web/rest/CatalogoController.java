package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.catalog.application.command.RegistrarEquipoCommand;
import com.biblioteca.catalog.application.command.RegistrarLibroCommand;
import com.biblioteca.catalog.application.command.RegistrarOtroCommand;
import com.biblioteca.catalog.application.usecase.DesactivarCatalogoItemUseCase;
import com.biblioteca.catalog.application.usecase.ListarCatalogoUseCase;
import com.biblioteca.catalog.application.usecase.ObtenerCatalogoItemUseCase;
import com.biblioteca.catalog.application.usecase.RegistrarEquipoUseCase;
import com.biblioteca.catalog.application.usecase.RegistrarLibroUseCase;
import com.biblioteca.catalog.application.usecase.RegistrarOtroUseCase;
import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.model.DetalleItem;
import com.biblioteca.catalog.domain.model.EquipoDetalle;
import com.biblioteca.catalog.domain.model.LibroDetalle;
import com.biblioteca.catalog.domain.model.OtroDetalle;
import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.catalog.domain.vo.Autor;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.ISBN;
import com.biblioteca.catalog.domain.vo.Titulo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/v1/catalogo")
public class CatalogoController {

    private final RegistrarLibroUseCase registrarLibro;
    private final RegistrarEquipoUseCase registrarEquipo;
    private final RegistrarOtroUseCase registrarOtro;
    private final ListarCatalogoUseCase listar;
    private final ObtenerCatalogoItemUseCase obtener;
    private final DesactivarCatalogoItemUseCase desactivar;

    public CatalogoController(RegistrarLibroUseCase registrarLibro,
                              RegistrarEquipoUseCase registrarEquipo,
                              RegistrarOtroUseCase registrarOtro,
                              ListarCatalogoUseCase listar,
                              ObtenerCatalogoItemUseCase obtener,
                              DesactivarCatalogoItemUseCase desactivar) {
        this.registrarLibro = registrarLibro;
        this.registrarEquipo = registrarEquipo;
        this.registrarOtro = registrarOtro;
        this.listar = listar;
        this.obtener = obtener;
        this.desactivar = desactivar;
    }

    @GetMapping
    public List<ItemResponse> listar(@RequestParam(required = false) String q,
                                     @RequestParam(required = false) TipoItem tipo) {
        return listar.ejecutar(q, tipo).stream().map(CatalogoController::aResponse).toList();
    }

    @GetMapping("/{id}")
    public ItemResponse obtener(@PathVariable UUID id) {
        return aResponse(obtener.ejecutar(new CatalogoItemId(id)));
    }

    @PostMapping("/libros")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse registrarLibro(@Valid @RequestBody LibroRequest request) {
        CatalogoItem item = registrarLibro.ejecutar(new RegistrarLibroCommand(
                new Titulo(request.titulo()),
                new ISBN(request.isbn()),
                new Autor(request.autor()),
                request.editorial(),
                request.anioPublicacion()));
        return aResponse(item);
    }

    @PostMapping("/equipos")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse registrarEquipo(@Valid @RequestBody EquipoRequest request) {
        CatalogoItem item = registrarEquipo.ejecutar(new RegistrarEquipoCommand(
                new Titulo(request.titulo()), request.marca(), request.modelo()));
        return aResponse(item);
    }

    @PostMapping("/otros")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse registrarOtro(@Valid @RequestBody OtroRequest request) {
        CatalogoItem item = registrarOtro.ejecutar(new RegistrarOtroCommand(
                new Titulo(request.titulo()), request.descripcion()));
        return aResponse(item);
    }

    @PostMapping("/{id}/desactivar")
    public ItemResponse desactivar(@PathVariable UUID id) {
        return aResponse(desactivar.ejecutar(new CatalogoItemId(id)));
    }

    private static ItemResponse aResponse(CatalogoItem item) {
        return new ItemResponse(item.id().value(), item.titulo().value(), item.tipo(),
                item.activo(), aDetalle(item.detalle()));
    }

    private static DetalleResponse aDetalle(DetalleItem detalle) {
        if (detalle instanceof LibroDetalle libro) {
            return new DetalleResponse(libro.isbn().value(), libro.autor().value(),
                    libro.editorial(), libro.anioPublicacion(), null, null, null);
        }
        if (detalle instanceof EquipoDetalle equipo) {
            return new DetalleResponse(null, null, null, null,
                    equipo.marca(), equipo.modelo(), null);
        }
        if (detalle instanceof OtroDetalle otro) {
            return new DetalleResponse(null, null, null, null, null, null, otro.descripcion());
        }
        throw new IllegalStateException("Tipo de detalle no soportado: " + detalle.getClass());
    }

    public record LibroRequest(
            @NotBlank String titulo,
            @NotBlank String isbn,
            @NotBlank String autor,
            @NotBlank String editorial,
            @Min(0) int anioPublicacion) {
    }

    public record EquipoRequest(
            @NotBlank String titulo,
            @NotBlank String marca,
            @NotBlank String modelo) {
    }

    public record OtroRequest(
            @NotBlank String titulo,
            @NotBlank String descripcion) {
    }

    public record ItemResponse(
            UUID id,
            String titulo,
            TipoItem tipo,
            boolean activo,
            DetalleResponse detalle) {
    }

    public record DetalleResponse(
            String isbn,
            String autor,
            String editorial,
            Integer anioPublicacion,
            String marca,
            String modelo,
            String descripcion) {
    }
}
