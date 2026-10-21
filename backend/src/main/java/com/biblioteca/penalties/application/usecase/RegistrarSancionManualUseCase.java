package com.biblioteca.penalties.application.usecase;

import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.application.command.RegistrarSancionManualCommand;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.penalties.domain.vo.SancionId;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class RegistrarSancionManualUseCase {

    private final SancionRepository sanciones;
    private final UsuarioRepository usuarios;
    private final DomainEventPublisher eventos;
    private final Clock clock;

    public RegistrarSancionManualUseCase(SancionRepository sanciones,
                                         UsuarioRepository usuarios,
                                         DomainEventPublisher eventos,
                                         Clock clock) {
        this.sanciones = sanciones;
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.clock = clock;
    }

    public Sancion ejecutar(RegistrarSancionManualCommand command) {
        Usuario operador = usuarios.buscarPorId(command.operadorId())
                .orElseThrow(() -> new NotFoundException("Operador", command.operadorId().value()));
        if (!operador.puedeGestionarSanciones()) {
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO",
                    "El operador no tiene el permiso GESTIONAR_SANCIONES");
        }
        UsuarioId usuarioId = command.usuarioId();
        usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario", usuarioId.value()));

        Sancion sancion = Sancion.manual(SancionId.nuevo(), usuarioId, command.prestamoId(),
                command.motivo(), command.diasSuspension(), command.multa(),
                LocalDate.now(clock), command.observaciones());
        Sancion guardada = sanciones.guardar(sancion);
        eventos.publicarTodos(sancion.extraerEventos());
        return guardada;
    }
}
