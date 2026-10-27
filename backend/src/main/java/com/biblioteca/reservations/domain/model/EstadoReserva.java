package com.biblioteca.reservations.domain.model;

public enum EstadoReserva {
    SOLICITADA,
    ACEPTADA, // Historico: aceptada con prestamo inmediato.
    APROBADA,
    ATENDIDA,
    EXPIRADA,
    DENEGADA,
    CANCELADA
}
