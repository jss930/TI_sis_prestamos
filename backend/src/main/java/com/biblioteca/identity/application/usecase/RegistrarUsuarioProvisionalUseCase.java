package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.application.command.RegistrarUsuarioCommand;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.repository.CredencialRepository;
import com.biblioteca.identity.domain.service.PasswordHasher;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrarUsuarioProvisionalUseCase {

    private final UsuarioRepository usuarios;
    private final DomainEventPublisher eventos;
    private final CredencialRepository credenciales;
    private final PasswordHasher passwordHasher;

    public RegistrarUsuarioProvisionalUseCase(UsuarioRepository usuarios, DomainEventPublisher eventos,
                                             CredencialRepository credenciales, PasswordHasher passwordHasher) {
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.credenciales = credenciales;
        this.passwordHasher = passwordHasher;
    }

    public Usuario ejecutar(RegistrarUsuarioCommand command) {
        if (usuarios.existePorEmail(command.email())) {
            throw new BusinessRuleException("EMAIL_DUPLICADO",
                    "Ya existe una cuenta con el correo " + command.email().value());
        }
        if (command.password() == null || command.password().length() < 8) {
            throw new DomainException("PASSWORD_INVALIDA", "La contraseña debe tener al menos 8 caracteres");
        }
        Usuario usuario = Usuario.registrarProvisional(
                command.email(), command.nombreCompleto(), command.perfil());
        usuario.actualizarDatosPendientes(command.email(), command.nombreCompleto(), command.perfil(), command.codigoInstitucional());
        if (usuarios.existePorCodigoInstitucional(command.codigoInstitucional()))
            throw new BusinessRuleException("CODIGO_INSTITUCIONAL_DUPLICADO", "Ya existe una cuenta con ese codigo institucional");
        Usuario guardado = usuarios.guardar(usuario);
        credenciales.guardar(usuario.id(), passwordHasher.hash(command.password()));
        eventos.publicarTodos(usuario.extraerEventos());
        return guardado;
    }
}
