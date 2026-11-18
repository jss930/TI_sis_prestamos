package com.biblioteca.circulation.application.command;

import com.biblioteca.circulation.domain.vo.PrestamoId;

public record RenovarPrestamoCommand(
        PrestamoId prestamoId,
        java.time.Instant nuevoVencimiento
) {
    public RenovarPrestamoCommand(PrestamoId prestamoId) { this(prestamoId, null); }
}
