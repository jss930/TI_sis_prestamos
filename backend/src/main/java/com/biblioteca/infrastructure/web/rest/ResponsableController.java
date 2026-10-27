package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.identity.application.usecase.GestionarResponsablesUseCase;
import com.biblioteca.identity.domain.model.CategoriaResponsable;
import com.biblioteca.identity.domain.model.CambioResponsable;
import com.biblioteca.identity.domain.model.CargoResponsable;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/responsables")
public class ResponsableController {
    private final GestionarResponsablesUseCase gestionar;
    private final UsuarioAutenticado autenticado;
    public ResponsableController(GestionarResponsablesUseCase gestionar, UsuarioAutenticado autenticado) {
        this.gestionar = gestionar;
        this.autenticado = autenticado;
    }
    @GetMapping
    public ResumenResponse listar(@RequestHeader(value = "Authorization", required = false) String token) {
        return resumen(autenticado.exigir(token));
    }
    @PutMapping("/{usuarioId}/asignaciones")
    public ResumenResponse confirmar(@PathVariable UUID usuarioId,
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody ConfirmacionRequest request) {
        UsuarioId operador = autenticado.exigir(token);
        gestionar.confirmar(operador, new UsuarioId(usuarioId), request.capacitacionVerificada(), request.categorias(), request.cargos());
        return resumen(operador);
    }
    @PutMapping("/{usuarioId}/capacitacion")
    public ResumenResponse capacitar(@PathVariable UUID usuarioId,
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody CapacitacionRequest request) {
        UsuarioId operador = autenticado.exigir(token);
        gestionar.capacitar(operador, new UsuarioId(usuarioId), request.verificada());
        return resumen(operador);
    }
    @PutMapping("/{usuarioId}/categorias/{categoria}")
    public ResumenResponse asignar(@PathVariable UUID usuarioId, @PathVariable CategoriaResponsable categoria,
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody AsignacionRequest request) {
        UsuarioId operador = autenticado.exigir(token);
        gestionar.asignar(operador, new UsuarioId(usuarioId), categoria, request.asignada(), request.cargo());
        return resumen(operador);
    }
    private ResumenResponse resumen(UsuarioId operador) {
        var datos = gestionar.consultar(operador);
        return new ResumenResponse(datos.responsables().stream().map(r -> new ResponsableResponse(
                r.usuarioId().value(), r.capacitacionVerificada(), r.categorias().stream().sorted().toList(), r.cargos())).toList(),
                datos.historial());
    }
    public record ConfirmacionRequest(@NotNull Boolean capacitacionVerificada,
                                      @NotNull java.util.Set<@NotNull CategoriaResponsable> categorias,
                                      java.util.Map<@NotNull CategoriaResponsable, @NotNull CargoResponsable> cargos) { }
    public record CapacitacionRequest(@NotNull Boolean verificada) { }
    public record AsignacionRequest(@NotNull Boolean asignada, CargoResponsable cargo) { }
    public record ResponsableResponse(UUID usuarioId, boolean capacitacionVerificada, List<CategoriaResponsable> categorias, java.util.Map<CategoriaResponsable, CargoResponsable> cargos) { }
    public record ResumenResponse(List<ResponsableResponse> responsables, List<CambioResponsable> historial) { }
}
