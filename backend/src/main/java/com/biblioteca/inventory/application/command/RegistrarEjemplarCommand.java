package com.biblioteca.inventory.application.command;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.inventory.domain.model.Ubicacion;
import com.biblioteca.inventory.domain.vo.Barcode;

public record RegistrarEjemplarCommand(
        CatalogoItemId catalogoItemId,
        Barcode barcode,
        Ubicacion ubicacion,
        String notas
) {
}
