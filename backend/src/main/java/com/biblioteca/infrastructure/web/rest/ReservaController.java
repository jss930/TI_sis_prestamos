package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.application.command.AceptarReservaCommand;
import com.biblioteca.reservations.application.command.CancelarReservaCommand;
import com.biblioteca.reservations.application.command.DenegarReservaCommand;
import com.biblioteca.reservations.application.command.SolicitarReservaCommand;
import com.biblioteca.reservations.application.usecase.AceptarReservaUseCase;
import com.biblioteca.reservations.application.usecase.CancelarReservaUseCase;
import com.biblioteca.reservations.application.usecase.DenegarReservaUseCase;
import com.biblioteca.reservations.application.usecase.ListarReservasUseCase;
import com.biblioteca.reservations.application.usecase.ObtenerReservaUseCase;
import com.biblioteca.reservations.application.usecase.SolicitarReservaUseCase;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final SolicitarReservaUseCase solicitar;
    private final AceptarReservaUseCase aceptar;
    private final DenegarReservaUseCase denegar;
    private final CancelarReservaUseCase cancelar;
    private final ListarReservasUseCase listar;
    private final ObtenerReservaUseCase obtener;

    public ReservaController(SolicitarReservaUseCase solicitar,
                             AceptarReservaUseCase aceptar,
                             DenegarReservaUseCase denegar,
                             CancelarReservaUseCase cancelar,
                             ListarReservasUseCase listar,
                             ObtenerReservaUseCase obtener) {
        this.solicitar = solicitar;
        this.aceptar = aceptar;
        this.denegar = denegar;
        this.cancelar = cancelar;
        this.listar = listar;
        this.obtener = obtener;
    }

    @GetMapping
    public List<ReservaResponse> listar(@RequestParam(required = false) UUID prestatarioId,
                                       @RequestParam(required = false) EstadoReserva estado) {
        UsuarioId usuarioId = prestatarioId == null ? null : new UsuarioId(prestatarioId);
        return listar.ejecutar(usuarioId, estado).stream()
                .map(ReservaController::aResponse).toList();
    }

    @GetMapping("/{id}")
    public ReservaResponse obtener(@PathVariable UUID id) {
        return aResponse(obtener.ejecutar(new ReservaId(id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse solicitar(@Valid @RequestBody SolicitarReservaRequest request) {
        Reserva reserva = solicitar.ejecutar(new SolicitarReservaCommand(
                new UsuarioId(request.prestatarioId()),
                new CatalogoItemId(request.catalogoItemId())));
        return aResponse(reserva);
    }

    @PostMapping("/{id}/aceptar")
    public ReservaResponse aceptar(@PathVariable UUID id,
                                  @Valid @RequestBody OperadorRequest request) {
        return aResponse(aceptar.ejecutar(new AceptarReservaCommand(
                new UsuarioId(request.operadorId()), new ReservaId(id))));
    }

    @PostMapping("/{id}/denegar")
    public ReservaResponse denegar(@PathVariable UUID id,
                                  @Valid @RequestBody DenegarRequest request) {
        return aResponse(denegar.ejecutar(new DenegarReservaCommand(
                new UsuarioId(request.operadorId()), new ReservaId(id), request.motivo())));
    }

    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable UUID id,
                                    @Valid @RequestBody CancelarRequest request) {
        return aResponse(cancelar.ejecutar(new CancelarReservaCommand(
                new UsuarioId(request.prestatarioId()), new ReservaId(id))));
    }

    private static ReservaResponse aResponse(Reserva reserva) {
        return new ReservaResponse(reserva.id().value(), reserva.prestatarioId().value(),
                reserva.catalogoItemId().value(), reserva.estado(), reserva.operadorId() == null
                ? null : reserva.operadorId().value(),
                reserva.prestamoId() == null ? null : reserva.prestamoId().value(),
                reserva.motivo(), reserva.fechaAlta(), reserva.fechaDecision());
    }

    public record SolicitarReservaRequest(
            @NotNull UUID prestatarioId,
            @NotNull UUID catalogoItemId) {
    }

    public record OperadorRequest(@NotNull UUID operadorId) {
    }

    public record DenegarRequest(
            @NotNull UUID operadorId,
            @NotNull String motivo) {
    }

    public record CancelarRequest(@NotNull UUID prestatarioId) {
    }

    public record ReservaResponse(
            UUID id,
            UUID prestatarioId,
            UUID catalogoItemId,
            EstadoReserva estado,
            UUID operadorId,
            UUID prestamoId,
            String motivo,
            Instant fechaAlta,
            Instant fechaDecision) {
    }
}
