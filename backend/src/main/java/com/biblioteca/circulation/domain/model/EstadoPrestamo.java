package com.biblioteca.circulation.domain.model;

/** Estado persistido del prestamo. "Vencido" es derivado de la fecha, no un estado. */
public enum EstadoPrestamo {
    ACTIVO,
    DEVUELTO,
    PERDIDO
}
