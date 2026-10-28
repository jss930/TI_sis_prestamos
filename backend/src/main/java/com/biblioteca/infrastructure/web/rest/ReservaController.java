package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.reservations.application.usecase.AccesoSolicitudes;
import com.biblioteca.reservations.application.usecase.EntregarSolicitudUseCase;
import com.biblioteca.reservations.application.usecase.ReprogramarReservaUseCase;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.security.UsuarioAutenticado;
import org.springframework.web.bind.annotation.RequestHeader;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final UsuarioAutenticado autenticado;
    private final AccesoSolicitudes acceso;
    private final EntregarSolicitudUseCase entregar;
    private final ReprogramarReservaUseCase reprogramar;

    public ReservaController(SolicitarReservaUseCase solicitar,
                             AceptarReservaUseCase aceptar,
                             DenegarReservaUseCase denegar,
                             CancelarReservaUseCase cancelar,
                             ListarReservasUseCase listar,
                             ObtenerReservaUseCase obtener,
                             UsuarioAutenticado autenticado,
                             AccesoSolicitudes acceso,
                             EntregarSolicitudUseCase entregar,
                             ReprogramarReservaUseCase reprogramar) {
        this.solicitar = solicitar;
        this.aceptar = aceptar;
        this.denegar = denegar;
        this.cancelar = cancelar;
        this.listar = listar;
        this.obtener = obtener;
        this.autenticado = autenticado;
        this.acceso = acceso;
        this.entregar = entregar;
        this.reprogramar = reprogramar;
    }

    @GetMapping
    public List<ReservaResponse> listar(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam(required = false) UUID prestatarioId,
                                       @RequestParam(required = false) EstadoReserva estado) {
        if (token == null) return List.of();
        UsuarioId actual = autenticado.exigir(token);
        UsuarioId usuarioId = prestatarioId == null ? null : new UsuarioId(prestatarioId);
        return listar.ejecutar(usuarioId, estado).stream()
                .filter(r -> r.prestatarioId().equals(actual) || acceso.puedeAtender(actual, r))
                .map(ReservaController::aResponse).toList();
    }

    @GetMapping("/{id}")
    public ReservaResponse obtener(@PathVariable UUID id, @RequestHeader(value = "Authorization", required = false) String token) {
        var reserva = obtener.ejecutar(new ReservaId(id));
        acceso.exigirLectura(autenticado.exigir(token), reserva);
        return aResponse(reserva);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse solicitar(@RequestHeader(value = "Authorization", required = false) String token, @Valid @RequestBody SolicitarReservaRequest request) {
        Reserva reserva = solicitar.ejecutar(new SolicitarReservaCommand(
                autenticado.exigir(token),
                new CatalogoItemId(request.catalogoItemId()), request.lugarUso() == null ? LugarUso.EN_CAMPUS : request.lugarUso(),
                request.inicioReserva(), request.finReserva()));
        return aResponse(reserva);
    }

    @PatchMapping("/{id}/horario")
    public ReservaResponse reprogramar(@PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody HorarioReservaRequest request) {
        return aResponse(reprogramar.ejecutar(autenticado.exigir(token), new ReservaId(id),
                request.inicioReserva(), request.finReserva()));
    }

    @PostMapping("/{id}/aceptar")
    public ReservaResponse aceptar(@PathVariable UUID id,
                                  @RequestHeader(value = "Authorization", required = false) String token) {
        return aResponse(aceptar.ejecutar(new AceptarReservaCommand(
                autenticado.exigir(token), new ReservaId(id))));
    }

    @PostMapping("/{id}/entregar")
    public ReservaResponse entregar(@PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return aResponse(entregar.ejecutar(autenticado.exigir(token), new ReservaId(id)));
    }

    @PostMapping("/{id}/denegar")
    public ReservaResponse denegar(@PathVariable UUID id,
                                  @RequestHeader(value = "Authorization", required = false) String token,
                                  @Valid @RequestBody DenegarRequest request) {
        return aResponse(denegar.ejecutar(new DenegarReservaCommand(
                autenticado.exigir(token), new ReservaId(id), request.motivo())));
    }

    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable UUID id,
                                    @RequestHeader(value = "Authorization", required = false) String token) {
        return aResponse(cancelar.ejecutar(new CancelarReservaCommand(
                autenticado.exigir(token), new ReservaId(id))));
    }

    private static ReservaResponse aResponse(Reserva reserva) {
        return new ReservaResponse(reserva.id().value(), reserva.prestatarioId().value(),
                reserva.catalogoItemId().value(), reserva.estado(), reserva.operadorId() == null
                ? null : reserva.operadorId().value(),
                reserva.prestamoId() == null ? null : reserva.prestamoId().value(),
                reserva.motivo(), reserva.fechaAlta(), reserva.fechaDecision(),
                reserva.ejemplarId() == null ? null : reserva.ejemplarId().value(), reserva.fechaLimiteRetiro(),
                reserva.fechaEntrega(), reserva.entregadoPorId() == null ? null : reserva.entregadoPorId().value(), reserva.lugarUso(),
                reserva.inicioReserva(), reserva.finReserva());
    }

    public record SolicitarReservaRequest(
            @NotNull UUID catalogoItemId, LugarUso lugarUso,
            @NotNull Instant inicioReserva, @NotNull Instant finReserva) {
    }

    public record HorarioReservaRequest(@NotNull Instant inicioReserva, @NotNull Instant finReserva) { }

    public record DenegarRequest(
            @NotNull String motivo) {
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
            Instant fechaDecision,
            UUID ejemplarId,
            Instant fechaLimiteRetiro,
            Instant fechaEntrega,
            UUID entregadoPorId,
            LugarUso lugarUso,
            Instant inicioReserva,
            Instant finReserva) {
    }
}
