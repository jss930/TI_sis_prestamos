package com.biblioteca.penalties.application.usecase;

import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.penalties.application.command.CumplirSancionCommand;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.shared.application.port.DomainEventPublisher;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CumplirSancionUseCase {

    private final SancionRepository sanciones;
    private final UsuarioRepository usuarios;
    private final DomainEventPublisher eventos;

    public CumplirSancionUseCase(SancionRepository sanciones,
                                 UsuarioRepository usuarios,
                                 DomainEventPublisher eventos) {
        this.sanciones = sanciones;
        this.usuarios = usuarios;
        this.eventos = eventos;
    }

    public Sancion ejecutar(CumplirSancionCommand command) {
        Usuario operador = usuarios.buscarPorId(command.operadorId())
                .orElseThrow(() -> new NotFoundException("Operador", command.operadorId().value()));
        if (!operador.puedeGestionarSanciones()) {
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO",
                    "El operador no tiene el permiso GESTIONAR_SANCIONES");
        }
        Sancion sancion = sanciones.buscarPorId(command.sancionId())
                .orElseThrow(() -> new NotFoundException("Sancion", command.sancionId().value()));
        sancion.cumplir();
        Sancion guardada = sanciones.guardar(sancion);
        eventos.publicarTodos(sancion.extraerEventos());
        return guardada;
    }
}
