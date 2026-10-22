package com.biblioteca.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CambioResponsable(UUID id, UUID usuarioId, UUID operadorId,
                                CategoriaResponsable categoria, Accion accion, Instant fecha) {
    public enum Accion { ASIGNADO, RETIRADO, CAPACITACION_VERIFICADA, CAPACITACION_RETIRADA }
}
