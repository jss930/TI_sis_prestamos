package com.biblioteca.circulation.application.usecase;

import com.biblioteca.circulation.application.command.RegistrarDevolucionCommand;
import com.biblioteca.circulation.domain.model.InspeccionDevolucion;
import com.biblioteca.circulation.domain.repository.InspeccionDevolucionRepository;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.List;

@Service
@Transactional
public class InspeccionarDevolucionUseCase {
    private final UsuarioRepository usuarios;
    private final PrestamoRepository prestamos;
    private final RegistrarDevolucionUseCase devolver;
    private final InspeccionDevolucionRepository inspecciones;
    private final Clock clock;

    public InspeccionarDevolucionUseCase(UsuarioRepository usuarios, PrestamoRepository prestamos,
            RegistrarDevolucionUseCase devolver, InspeccionDevolucionRepository inspecciones, Clock clock) {
        this.usuarios = usuarios;
        this.prestamos = prestamos;
        this.devolver = devolver;
        this.inspecciones = inspecciones;
        this.clock = clock;
    }

    public InspeccionDevolucion registrar(UsuarioId operadorId, PrestamoId prestamoId,
            EstadoFisico estadoFisico, boolean accesoriosFaltantes, String observaciones) {
        exigirOperador(operadorId);
        // Serializa las recepciones del mismo prestamo; devolucion y auditoria son atomicas.
        prestamos.buscarPorIdParaActualizar(prestamoId)
                .orElseThrow(() -> new NotFoundException("Prestamo", prestamoId.value()));
        InspeccionDevolucion inspeccion = new InspeccionDevolucion(prestamoId.value(),
                operadorId.value(), estadoFisico, accesoriosFaltantes, observaciones, clock.instant());
        devolver.ejecutar(new RegistrarDevolucionCommand(prestamoId, inspeccion.estadoResultante()));
        inspecciones.guardar(inspeccion);
        return inspeccion;
    }

    @Transactional(readOnly = true)
    public List<InspeccionDevolucion> listar(UsuarioId operadorId) {
        exigirOperador(operadorId);
        return inspecciones.listar();
    }

    private void exigirOperador(UsuarioId id) {
        var usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Usuario", id.value()));
        if (!usuario.puedeRegistrarDevolucion()) {
            throw new BusinessRuleException("OPERADOR_NO_HABILITADO",
                    "Se requiere una cuenta validada con permiso REGISTRAR_DEVOLUCION");
        }
    }
}
