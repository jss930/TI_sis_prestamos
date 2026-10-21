package com.biblioteca.catalog.application.command;

import com.biblioteca.catalog.domain.vo.Titulo;

public record RegistrarEquipoCommand(
        Titulo titulo,
        String marca,
        String modelo
) {
}
