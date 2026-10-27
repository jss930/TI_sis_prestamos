package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.identity.application.usecase.RevisarCuentaUseCase;
import com.biblioteca.identity.domain.model.PerfilPrestatario;
import com.biblioteca.identity.domain.vo.*;
import com.biblioteca.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
public class RevisionCuentaController {
    private final RevisarCuentaUseCase revisar;
    private final UsuarioAutenticado autenticado;
    public RevisionCuentaController(RevisarCuentaUseCase revisar, UsuarioAutenticado autenticado) { this.revisar=revisar; this.autenticado=autenticado; }

    @PostMapping("/{id}/solicitar-correccion")
    public UsuarioController.UsuarioResponse solicitar(@PathVariable UUID id,
            @RequestHeader(value="Authorization",required=false) String token, @Valid @RequestBody ObservacionesRequest request) {
        return UsuarioController.aResponse(revisar.solicitarCorreccion(autenticado.exigir(token), new UsuarioId(id), request.observaciones()));
    }
    @PatchMapping("/{id}/datos-pendientes")
    public UsuarioController.UsuarioResponse actualizar(@PathVariable UUID id,
            @RequestHeader(value="Authorization",required=false) String token, @Valid @RequestBody DatosRequest request) {
        return UsuarioController.aResponse(revisar.actualizar(autenticado.exigir(token), new UsuarioId(id),
                new EmailAddress(request.email()), new NombreCompleto(request.nombreCompleto()), request.perfil(), new CodigoInstitucional(request.codigoInstitucional())));
    }
    public record ObservacionesRequest(@NotBlank @Size(max=1000) String observaciones) { }
    public record DatosRequest(@NotBlank @Email String email, @NotBlank String nombreCompleto,
                               @NotNull PerfilPrestatario perfil, @NotBlank @Size(max=30) String codigoInstitucional) { }
}
