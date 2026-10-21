package com.biblioteca.catalog.application.command;

import com.biblioteca.catalog.domain.vo.Autor;
import com.biblioteca.catalog.domain.vo.ISBN;
import com.biblioteca.catalog.domain.vo.Titulo;

public record RegistrarLibroCommand(
        Titulo titulo,
        ISBN isbn,
        Autor autor,
        String editorial,
        int anioPublicacion
) {
}
