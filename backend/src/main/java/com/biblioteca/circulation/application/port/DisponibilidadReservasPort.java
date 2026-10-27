package com.biblioteca.circulation.application.port;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;

import java.time.Instant;

/** Evita que un préstamo directo invada un horario ya aprobado. */
public interface DisponibilidadReservasPort {
    boolean existeConflicto(EjemplarId ejemplarId, UsuarioId prestatarioId,
                            Instant inicio, Instant fin);
}
