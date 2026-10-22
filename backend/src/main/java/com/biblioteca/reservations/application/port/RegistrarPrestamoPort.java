package com.biblioteca.reservations.application.port;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;

/**
 * Puerto de aplicacion hacia circulation. La infraestructura implementa este
 * puerto delegando en RegistrarPrestamoUseCase, de modo que reservations no
 * depende del dominio ni de los casos de uso de circulation.
 */
public interface RegistrarPrestamoPort {

    default PrestamoId registrarPrestamo(UsuarioId operadorId, UsuarioId prestatarioId,
                                        CatalogoItemId catalogoItemId) {
        return registrarPrestamo(operadorId, prestatarioId, catalogoItemId, null);
    }

    PrestamoId registrarPrestamo(UsuarioId operadorId, UsuarioId prestatarioId,
            CatalogoItemId catalogoItemId, com.biblioteca.inventory.domain.vo.EjemplarId ejemplarId);
}
