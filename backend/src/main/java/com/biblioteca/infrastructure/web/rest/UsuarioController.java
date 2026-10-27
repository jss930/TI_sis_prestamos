package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.identity.application.command.OtorgarPermisoCommand;
import com.biblioteca.identity.application.command.ReactivarUsuarioCommand;
import com.biblioteca.identity.application.command.RegistrarUsuarioCommand;
import com.biblioteca.identity.application.command.RevocarPermisoCommand;
import com.biblioteca.identity.application.command.SuspenderUsuarioCommand;
import com.biblioteca.identity.application.command.ValidarUsuarioCommand;
import com.biblioteca.identity.application.usecase.ListarUsuariosUseCase;
import com.biblioteca.identity.application.usecase.ObtenerUsuarioUseCase;
import com.biblioteca.identity.application.usecase.OtorgarPermisoUseCase;
import com.biblioteca.identity.application.usecase.ReactivarUsuarioUseCase;
import com.biblioteca.identity.application.usecase.RegistrarUsuarioProvisionalUseCase;
import com.biblioteca.identity.application.usecase.RevocarPermisoUseCase;
import com.biblioteca.identity.application.usecase.SuspenderUsuarioUseCase;
import com.biblioteca.identity.application.usecase.ValidarUsuarioUseCase;
import com.biblioteca.identity.domain.model.EstadoCuentaUsuario;
import com.biblioteca.identity.domain.model.PerfilPrestatario;
import com.biblioteca.identity.domain.model.PermisoOperativo;
import com.biblioteca.identity.domain.model.RolAcceso;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.NombreCompleto;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final RegistrarUsuarioProvisionalUseCase registrar;
    private final ValidarUsuarioUseCase validar;
    private final SuspenderUsuarioUseCase suspender;
    private final ReactivarUsuarioUseCase reactivar;
    private final OtorgarPermisoUseCase otorgarPermiso;
    private final RevocarPermisoUseCase revocarPermiso;
    private final ListarUsuariosUseCase listar;
    private final ObtenerUsuarioUseCase obtener;
    private final UsuarioAutenticado autenticado;

    public UsuarioController(RegistrarUsuarioProvisionalUseCase registrar,
                             ValidarUsuarioUseCase validar,
                             SuspenderUsuarioUseCase suspender,
                             ReactivarUsuarioUseCase reactivar,
                             OtorgarPermisoUseCase otorgarPermiso,
                             RevocarPermisoUseCase revocarPermiso,
                             ListarUsuariosUseCase listar,
                             ObtenerUsuarioUseCase obtener,
                             UsuarioAutenticado autenticado) {
        this.registrar = registrar;
        this.validar = validar;
        this.suspender = suspender;
        this.reactivar = reactivar;
        this.otorgarPermiso = otorgarPermiso;
        this.revocarPermiso = revocarPermiso;
        this.listar = listar;
        this.obtener = obtener;
        this.autenticado = autenticado;
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam(required = false) String q,
                                        @RequestParam(required = false) EstadoCuentaUsuario estado,
                                        @RequestParam(required = false) PerfilPrestatario perfil) {
        Usuario actor = token == null ? null : obtener.ejecutar(autenticado.exigir(token));
        return listar.ejecutar(q, estado, perfil).stream()
                .map(usuario -> aResponse(usuario, puedeVerRevision(actor, usuario))).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable UUID id, @RequestHeader(value = "Authorization", required = false) String token) {
        Usuario actor = token == null ? null : obtener.ejecutar(autenticado.exigir(token));
        Usuario usuario = obtener.ejecutar(new UsuarioId(id));
        return aResponse(usuario, puedeVerRevision(actor, usuario));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistrarUsuarioRequest request) {
        Usuario usuario = registrar.ejecutar(new RegistrarUsuarioCommand(
                new EmailAddress(request.email()),
                new NombreCompleto(request.nombreCompleto()),
                request.perfil(), request.password(), new com.biblioteca.identity.domain.vo.CodigoInstitucional(request.codigoInstitucional())));
        return aResponse(usuario);
    }

    @PostMapping("/{id}/validar")
    public UsuarioResponse validar(@PathVariable UUID id, @RequestHeader(value = "Authorization", required = false) String token, @Valid @RequestBody ValidacionRequest request) {
        return aResponse(validar.ejecutar(new ValidarUsuarioCommand(
                autenticado.exigir(token), new UsuarioId(id), request.datosRevisados())));
    }

    @PostMapping("/{id}/suspender")
    public UsuarioResponse suspender(@PathVariable UUID id, @RequestHeader(value = "Authorization", required = false) String token, @Valid @RequestBody SuspenderRequest request) {
        return aResponse(suspender.ejecutar(new SuspenderUsuarioCommand(
                autenticado.exigir(token), new UsuarioId(id), request.motivo())));
    }

    @PostMapping("/{id}/reactivar")
    public UsuarioResponse reactivar(@PathVariable UUID id, @RequestHeader(value = "Authorization", required = false) String token, @Valid @RequestBody OperadorRequest request) {
        return aResponse(reactivar.ejecutar(new ReactivarUsuarioCommand(
                autenticado.exigir(token), new UsuarioId(id))));
    }

    @PostMapping("/{id}/permisos")
    public UsuarioResponse otorgarPermiso(@PathVariable UUID id,
                                          @RequestHeader(value = "Authorization", required = false) String token,
                                          @Valid @RequestBody PermisoRequest request) {
        return aResponse(otorgarPermiso.ejecutar(new OtorgarPermisoCommand(
                autenticado.exigir(token), new UsuarioId(id), request.permiso())));
    }

    @DeleteMapping("/{id}/permisos/{permiso}")
    public UsuarioResponse revocarPermiso(@PathVariable UUID id,
                                          @PathVariable PermisoOperativo permiso,
                                          @RequestParam UUID operadorId,
                                          @RequestHeader(value = "Authorization", required = false) String token) {
        return aResponse(revocarPermiso.ejecutar(new RevocarPermisoCommand(
                autenticado.exigir(token), new UsuarioId(id), permiso)));
    }

    private static boolean puedeVerRevision(Usuario actor, Usuario usuario) {
        return actor != null && (actor.id().equals(usuario.id()) || actor.puedeAdministrarSistema() || actor.puedeValidarProvisionales());
    }

    static UsuarioResponse aResponse(Usuario usuario) { return aResponse(usuario, true); }

    private static UsuarioResponse aResponse(Usuario usuario, boolean revisionVisible) {
        return new UsuarioResponse(usuario.id().value(), usuario.email().value(),
                usuario.nombreCompleto().value(), usuario.estado(), usuario.perfil(),
                usuario.permisos(), usuario.fechaAlta(), usuario.fechaValidacion(),
                usuario.motivoSuspension(), usuario.rolAcceso(),
                !revisionVisible || usuario.codigoInstitucional() == null ? null : usuario.codigoInstitucional().value(),
                !revisionVisible || usuario.validadoPorId() == null ? null : usuario.validadoPorId().value(), revisionVisible ? usuario.observacionesRevision() : null,
                !revisionVisible || usuario.revisadoPorId() == null ? null : usuario.revisadoPorId().value(), revisionVisible ? usuario.fechaRevision() : null, revisionVisible && usuario.correccionPendiente());
    }

    public record RegistrarUsuarioRequest(
            @NotBlank @Email String email,
            @NotBlank String nombreCompleto,
            @NotNull PerfilPrestatario perfil,
            @jakarta.validation.constraints.Size(min = 8, max = 128) @NotBlank String password,
            @NotBlank @jakarta.validation.constraints.Size(max = 30) String codigoInstitucional) {
    }

    public record ValidacionRequest(@jakarta.validation.constraints.AssertTrue(message = "Confirma la revision de los datos") boolean datosRevisados) { }

    public record OperadorRequest(@NotNull UUID operadorId) {
    }

    public record SuspenderRequest(
            @NotNull UUID operadorId,
            @NotBlank String motivo) {
    }

    public record PermisoRequest(
            @NotNull UUID operadorId,
            @NotNull PermisoOperativo permiso) {
    }

    public record UsuarioResponse(
            UUID id,
            String email,
            String nombreCompleto,
            EstadoCuentaUsuario estado,
            PerfilPrestatario perfil,
            Set<PermisoOperativo> permisos,
            Instant fechaAlta,
            Instant fechaValidacion,
            String motivoSuspension,
            RolAcceso rolAcceso,
            String codigoInstitucional,
            UUID validadoPorId,
            String observacionesRevision,
            UUID revisadoPorId,
            Instant fechaRevision,
            boolean correccionPendiente) {
    }
}
