package com.biblioteca.catalog.application.command;

import com.biblioteca.catalog.domain.vo.Titulo;

public record RegistrarOtroCommand(
        Titulo titulo,
        String descripcion
) {
}
