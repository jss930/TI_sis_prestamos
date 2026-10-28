package com.biblioteca.reservations.application.port;

import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.reservations.domain.vo.ReservaId;

import java.time.Instant;

/** Consulta si una copia está libre durante un intervalo futuro completo. */
public interface DisponibilidadProgramadaPort {
    boolean estaLibre(EjemplarId ejemplarId, Instant inicio, Instant fin, ReservaId excluir);
}
