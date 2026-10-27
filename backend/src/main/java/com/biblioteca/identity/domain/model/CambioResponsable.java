package com.biblioteca.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CambioResponsable(UUID id, UUID usuarioId, UUID operadorId,
                                CategoriaResponsable categoria, Accion accion, Instant fecha, CargoResponsable cargo) {
    public CambioResponsable(UUID id, UUID usuarioId, UUID operadorId, CategoriaResponsable categoria, Accion accion, Instant fecha) {
        this(id, usuarioId, operadorId, categoria, accion, fecha, null);
    }
    public enum Accion { ASIGNADO, RETIRADO, CARGO_CAMBIADO, CAPACITACION_VERIFICADA, CAPACITACION_RETIRADA }
}
