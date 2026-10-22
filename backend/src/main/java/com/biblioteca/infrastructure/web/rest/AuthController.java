package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.identity.application.usecase.IniciarSesionUseCase;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.infrastructure.security.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IniciarSesionUseCase iniciarSesion;
    private final TokenService tokens;

    public AuthController(IniciarSesionUseCase iniciarSesion, TokenService tokens) {
        this.iniciarSesion = iniciarSesion;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = iniciarSesion.ejecutar(new EmailAddress(request.email()), request.password());
        String token = tokens.generar(usuario.id().value(), usuario.email().value());
        return new LoginResponse(token, new UsuarioResponse(
                usuario.id().value(),
                usuario.email().value(),
                usuario.nombreCompleto().value(),
                usuario.estado().name(),
                usuario.perfil().name(),
                usuario.permisos().stream().map(Enum::name).toList(),
                usuario.fechaAlta().toString(),
                usuario.fechaValidacion() == null ? null : usuario.fechaValidacion().toString(),
                usuario.motivoSuspension(),
                usuario.rolAcceso().name()));
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {
    }

    public record UsuarioResponse(
            UUID id,
            String email,
            String nombreCompleto,
            String estado,
            String perfil,
            java.util.List<String> permisos,
            String fechaAlta,
            String fechaValidacion,
            String motivoSuspension,
            String rolAcceso) {
    }

    public record LoginResponse(String token, UsuarioResponse usuario) {
    }
}
