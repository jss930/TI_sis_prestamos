package com.biblioteca.reservations.application.port;

import com.biblioteca.identity.domain.vo.UsuarioId;
import java.time.LocalDate;

/** Consulta de prestamos activos requerida para aplicar el limite combinado de operaciones. */
public interface ContarPrestamosActivosPort {
    long contar(UsuarioId prestatarioId);

    default boolean tieneVencido(UsuarioId prestatarioId, LocalDate fecha) {
        return false;
    }
}
