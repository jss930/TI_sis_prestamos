package com.biblioteca.circulation.application.command;

import com.biblioteca.circulation.domain.vo.PrestamoId;

public record RenovarPrestamoCommand(
        PrestamoId prestamoId
) {
}
