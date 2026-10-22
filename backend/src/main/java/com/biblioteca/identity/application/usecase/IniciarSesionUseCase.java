package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.CredencialRepository;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.service.PasswordHasher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class IniciarSesionUseCase {

    private final UsuarioRepository usuarios;
    private final CredencialRepository credenciales;
    private final PasswordHasher hasher;

    public IniciarSesionUseCase(UsuarioRepository usuarios,
                                CredencialRepository credenciales,
                                PasswordHasher hasher) {
        this.usuarios = usuarios;
        this.credenciales = credenciales;
        this.hasher = hasher;
    }

    public Usuario ejecutar(EmailAddress email, String password) {
        Objects.requireNonNull(email, "El correo es obligatorio");
        Objects.requireNonNull(password, "La contraseña es obligatoria");
        Usuario usuario = usuarios.buscarPorEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario", email.value()));
        String hash = credenciales.buscarHashPorUsuario(usuario.id())
                .orElseThrow(() -> new BusinessRuleException("CREDENCIALES_INVALIDAS",
                        "Credenciales invalidas"));
        if (!hasher.matches(password, hash)) {
            throw new BusinessRuleException("CREDENCIALES_INVALIDAS", "Credenciales invalidas");
        }
        return usuario;
    }
}
