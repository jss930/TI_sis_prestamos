package com.biblioteca.circulation.application.port;

import com.biblioteca.identity.domain.vo.UsuarioId;

/** Consulta de reservas activas requerida para aplicar el limite combinado de operaciones. */
public interface ContarReservasActivasPort {
    long contar(UsuarioId prestatarioId);
}
