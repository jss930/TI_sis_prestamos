package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.circulation.application.command.RegistrarDevolucionCommand;
import com.biblioteca.circulation.application.command.RegistrarPrestamoCommand;
import com.biblioteca.circulation.application.command.RenovarPrestamoCommand;
import com.biblioteca.circulation.application.usecase.ListarPrestamosUseCase;
import com.biblioteca.circulation.application.usecase.ObtenerPrestamoUseCase;
import com.biblioteca.circulation.application.usecase.RegistrarDevolucionUseCase;
import com.biblioteca.circulation.application.usecase.RegistrarPrestamoUseCase;
import com.biblioteca.circulation.application.usecase.RenovarPrestamoUseCase;
import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.security.UsuarioAutenticado;
import org.springframework.web.bind.annotation.RequestHeader;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import jakarta.validation.Valid;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prestamos")
public class PrestamoController {

    private final RegistrarPrestamoUseCase registrar;
    private final RegistrarDevolucionUseCase devolver;
    private final RenovarPrestamoUseCase renovar;
    private final ListarPrestamosUseCase listar;
    private final ObtenerPrestamoUseCase obtener;
    private final UsuarioAutenticado autenticado;
    private final com.biblioteca.identity.domain.repository.UsuarioRepository usuarios;

    public PrestamoController(RegistrarPrestamoUseCase registrar,
                              RegistrarDevolucionUseCase devolver,
                              RenovarPrestamoUseCase renovar,
                              ListarPrestamosUseCase listar,
                              ObtenerPrestamoUseCase obtener, UsuarioAutenticado autenticado,
                              com.biblioteca.identity.domain.repository.UsuarioRepository usuarios) {
        this.registrar = registrar;
        this.devolver = devolver;
        this.renovar = renovar;
        this.listar = listar;
        this.obtener = obtener;
        this.autenticado = autenticado;
        this.usuarios = usuarios;
    }

    @GetMapping
    public List<PrestamoResponse> listar(@RequestParam(required = false) UUID prestatarioId,
                                         @RequestParam(required = false) EstadoPrestamo estado) {
        UsuarioId usuarioId = prestatarioId == null ? null : new UsuarioId(prestatarioId);
        return listar.ejecutar(usuarioId, estado).stream()
                .map(PrestamoController::aResponse).toList();
    }

    @GetMapping("/{id}")
    public PrestamoResponse obtener(@PathVariable UUID id) {
        return aResponse(obtener.ejecutar(new PrestamoId(id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestamoResponse registrar(@RequestHeader(value = "Authorization", required = false) String token, @Valid @RequestBody RegistrarPrestamoRequest request) {
        Prestamo prestamo = registrar.ejecutar(new RegistrarPrestamoCommand(
                autenticado.exigir(token),
                new UsuarioId(request.prestatarioId()),
                new EjemplarId(request.ejemplarId()),
                request.lugarUso()));
        return aResponse(prestamo);
    }

    @PostMapping("/{id}/devolucion")
    public PrestamoResponse devolver(@PathVariable UUID id,
                                     @RequestHeader(value = "Authorization", required = false) String token,
                                     @Valid @RequestBody DevolucionRequest request) {
        var actor = usuarios.buscarPorId(autenticado.exigir(token)).orElseThrow();
        if (!actor.puedeRegistrarDevolucion()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "No tienes permiso para registrar devoluciones");
        return aResponse(devolver.ejecutar(new RegistrarDevolucionCommand(
                new PrestamoId(id), request.estadoFisico())));
    }

    @PostMapping("/{id}/renovacion")
    public PrestamoResponse renovar(@PathVariable UUID id) {
        return aResponse(renovar.ejecutar(new RenovarPrestamoCommand(new PrestamoId(id))));
    }

    private static PrestamoResponse aResponse(Prestamo prestamo) {
        LocalDate hoy = LocalDate.now();
        return new PrestamoResponse(prestamo.id().value(), prestamo.operadorId().value(),
                prestamo.prestatarioId().value(), prestamo.ejemplarId().value(),
                prestamo.periodo().inicio(), prestamo.periodo().vencimiento(),
                prestamo.fechaDevolucion(), prestamo.lugarUso(), prestamo.estado(),
                prestamo.renovaciones(), prestamo.estaVencido(hoy), prestamo.diasRetraso(hoy));
    }

    public record RegistrarPrestamoRequest(
            @NotNull UUID operadorId,
            @NotNull UUID prestatarioId,
            @NotNull UUID ejemplarId,
            @NotNull LugarUso lugarUso) {
    }

    public record DevolucionRequest(@NotNull EstadoFisico estadoFisico) {
    }

    public record PrestamoResponse(
            UUID id,
            UUID operadorId,
            UUID prestatarioId,
            UUID ejemplarId,
            LocalDate fechaInicio,
            LocalDate fechaVencimiento,
            LocalDate fechaDevolucion,
            LugarUso lugarUso,
            EstadoPrestamo estado,
            int renovaciones,
            boolean vencido,
            int diasRetraso) {
    }
}
