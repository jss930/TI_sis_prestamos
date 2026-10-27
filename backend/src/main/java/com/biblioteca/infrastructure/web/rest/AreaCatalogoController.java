package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.administration.application.usecase.AsignarAreaCatalogoUseCase;
import com.biblioteca.catalog.domain.model.AreaCatalogo;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalogo")
public class AreaCatalogoController {
    private final AsignarAreaCatalogoUseCase asignar;
    private final UsuarioAutenticado autenticado;
    public AreaCatalogoController(AsignarAreaCatalogoUseCase asignar, UsuarioAutenticado autenticado) {
        this.asignar = asignar; this.autenticado = autenticado;
    }
    @PatchMapping("/{id}/area")
    public void asignar(@PathVariable UUID id, @RequestHeader(value="Authorization", required=false) String token,
                        @Valid @RequestBody AreaRequest request) {
        asignar.ejecutar(autenticado.exigir(token), new CatalogoItemId(id), request.area());
    }
    public record AreaRequest(@NotNull AreaCatalogo area) { }
}
