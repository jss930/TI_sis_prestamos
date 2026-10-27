package com.biblioteca.identity.application.usecase;

import com.biblioteca.identity.application.command.ValidarUsuarioCommand;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ValidarUsuarioUseCase {

    private final UsuarioRepository usuarios;
    private final DomainEventPublisher eventos;
    private final java.time.Clock clock;

    public ValidarUsuarioUseCase(UsuarioRepository usuarios, DomainEventPublisher eventos) {
        this(usuarios, eventos, java.time.Clock.systemUTC());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ValidarUsuarioUseCase(UsuarioRepository usuarios, DomainEventPublisher eventos, java.time.Clock clock) {
        this.usuarios = usuarios;
        this.clock = clock;
        this.eventos = eventos;
    }

    public Usuario ejecutar(ValidarUsuarioCommand command) {
        if (command.operadorId().equals(command.usuarioId())) {
            throw new BusinessRuleException("AUTOVALIDACION_NO_PERMITIDA",
                    "Un operador no puede validar su propia cuenta");
        }
        Usuario operador = usuarios.buscarPorId(command.operadorId())
                .orElseThrow(() -> new NotFoundException("Operador", command.operadorId().value()));
        if (!operador.puedeValidarProvisionales() && !operador.puedeAdministrarSistema()) {
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO",
                    "El operador no tiene el permiso VALIDAR_USUARIO_PROVISIONAL");
        }
        Usuario usuario = usuarios.buscarPorIdParaActualizar(command.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario", command.usuarioId().value()));
        usuario.validar(command.operadorId(), clock.instant(), command.datosRevisados());
        Usuario guardado = usuarios.guardar(usuario);
        eventos.publicarTodos(usuario.extraerEventos());
        return guardado;
    }
}
