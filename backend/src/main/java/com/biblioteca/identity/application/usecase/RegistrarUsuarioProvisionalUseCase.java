package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.application.command.RegistrarUsuarioCommand;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import org.springframework.stereotype.Service;

@Service
public class RegistrarUsuarioProvisionalUseCase {

    private final UsuarioRepository usuarios;
    private final DomainEventPublisher eventos;

    public RegistrarUsuarioProvisionalUseCase(UsuarioRepository usuarios, DomainEventPublisher eventos) {
        this.usuarios = usuarios;
        this.eventos = eventos;
    }

    public Usuario ejecutar(RegistrarUsuarioCommand command) {
        if (usuarios.existePorEmail(command.email())) {
            throw new BusinessRuleException("EMAIL_DUPLICADO",
                    "Ya existe una cuenta con el correo " + command.email().value());
        }
        Usuario usuario = Usuario.registrarProvisional(
                command.email(), command.nombreCompleto(), command.perfil());
        Usuario guardado = usuarios.guardar(usuario);
        eventos.publicarTodos(usuario.extraerEventos());
        return guardado;
    }
}
