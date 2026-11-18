package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.application.command.CumplirSancionCommand;
import com.biblioteca.penalties.application.command.RegistrarSancionManualCommand;
import com.biblioteca.penalties.application.usecase.CumplirSancionUseCase;
import com.biblioteca.penalties.application.usecase.ListarSancionesUseCase;
import com.biblioteca.penalties.application.usecase.RegistrarSancionManualUseCase;
import com.biblioteca.penalties.domain.model.EstadoSancion;
import com.biblioteca.penalties.domain.model.MotivoSancion;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.vo.MontoMulta;
import com.biblioteca.penalties.domain.vo.SancionId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sanciones")
public class SancionController {

    private final ListarSancionesUseCase listar;
    private final CumplirSancionUseCase cumplir;
    private final RegistrarSancionManualUseCase registrarManual;
    private final com.biblioteca.infrastructure.security.AccesoHistorial acceso;

    public SancionController(ListarSancionesUseCase listar,
                             CumplirSancionUseCase cumplir,
                              RegistrarSancionManualUseCase registrarManual,
                              com.biblioteca.infrastructure.security.AccesoHistorial acceso) {
        this.listar = listar;
        this.cumplir = cumplir;
        this.registrarManual = registrarManual;
        this.acceso = acceso;
    }

    @GetMapping
    public List<SancionResponse> listar(@org.springframework.web.bind.annotation.RequestHeader(
                                        value = "Authorization", required = false) String token,
                                        @RequestParam(required = false) UUID usuarioId,
                                        @RequestParam(required = false) EstadoSancion estado) {
        var actor = acceso.actor(token);
        UsuarioId id = usuarioId == null ? null : new UsuarioId(usuarioId);
        if (!actor.puedeAdministrarSistema() && !actor.puedeGestionarSanciones()) {
            if (id != null && !id.equals(actor.id())) acceso.prohibido();
            id = actor.id();
        }
        return listar.ejecutar(id, estado).stream()
                .map(SancionController::aResponse).toList();
    }

    @PostMapping("/{id}/cumplir")
    public SancionResponse cumplir(@PathVariable UUID id,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody(required = false) OperadorRequest request) {
        var actor = acceso.actor(token);
        if (!actor.puedeGestionarSanciones()) acceso.prohibido();
        return aResponse(cumplir.ejecutar(new CumplirSancionCommand(
                actor.id(), new SancionId(id))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SancionResponse registrarManual(
            @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody SancionManualRequest request) {
        var actor = acceso.actor(token);
        if (!actor.puedeGestionarSanciones()) acceso.prohibido();
        Sancion sancion = registrarManual.ejecutar(new RegistrarSancionManualCommand(
                actor.id(),
                new UsuarioId(request.usuarioId()),
                request.prestamoId() == null ? null : new PrestamoId(request.prestamoId()),
                request.motivo(),
                request.diasSuspension(),
                MontoMulta.deSoles(request.multa()),
                request.observaciones()));
        return aResponse(sancion);
    }

    private static SancionResponse aResponse(Sancion sancion) {
        return new SancionResponse(sancion.id().value(), sancion.usuarioId().value(),
                sancion.prestamoId() == null ? null : sancion.prestamoId().value(),
                sancion.motivo(), sancion.diasSuspension(), sancion.multa().monto(),
                sancion.multa().moneda(), sancion.fechaInicio(), sancion.fechaFin(),
                sancion.estado(), sancion.observaciones());
    }

    public record OperadorRequest(@NotNull UUID operadorId) {
    }

    public record SancionManualRequest(
            UUID operadorId,
            @NotNull UUID usuarioId,
            UUID prestamoId,
            @NotNull MotivoSancion motivo,
            @Min(0) int diasSuspension,
            @NotNull @DecimalMin("0.00") BigDecimal multa,
            String observaciones) {
    }

    public record SancionResponse(
            UUID id,
            UUID usuarioId,
            UUID prestamoId,
            MotivoSancion motivo,
            int diasSuspension,
            BigDecimal multa,
            String moneda,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            EstadoSancion estado,
            String observaciones) {
    }
}
