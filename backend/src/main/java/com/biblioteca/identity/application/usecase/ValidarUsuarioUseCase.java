package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.application.command.ValidarUsuarioCommand;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ValidarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final DomainEventPublisher eventos;

    public ValidarUsuarioUseCase(UsuarioRepository usuarios, DomainEventPublisher eventos) {
        this.usuarios = usuarios;
        this.eventos = eventos;
    }

    public Usuario ejecutar(ValidarUsuarioCommand command) {
        Usuario operador = usuarios.buscarPorId(command.operadorId())
                .orElseThrow(() -> new NotFoundException("Operador", command.operadorId().value()));
        if (!operador.puedeValidarProvisionales()) {
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO",
                    "El operador no tiene el permiso VALIDAR_USUARIO_PROVISIONAL");
        }
        Usuario usuario = usuarios.buscarPorId(command.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario", command.usuarioId().value()));
        usuario.validar();
        Usuario guardado = usuarios.guardar(usuario);
        eventos.publicarTodos(usuario.extraerEventos());
        return guardado;
    }
}
