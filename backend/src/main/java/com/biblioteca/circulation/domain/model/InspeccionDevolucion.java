package com.biblioteca.circulation.domain.model;

import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.shared.domain.DomainException;
import java.time.Instant;
import java.util.UUID;

public record InspeccionDevolucion(UUID prestamoId, UUID recibidoPorId, EstadoFisico estadoFisico,
        boolean accesoriosFaltantes, String observaciones, Instant fecha) {
    public InspeccionDevolucion {
        if (estadoFisico != EstadoFisico.DISPONIBLE && estadoFisico != EstadoFisico.DANADO
                && estadoFisico != EstadoFisico.PERDIDO) {
            throw new DomainException("ESTADO_DEVOLUCION_INVALIDO", "Selecciona bueno, danado o perdido");
        }
        observaciones = observaciones == null ? "" : observaciones.trim();
        if (observaciones.length() > 1000) {
            throw new DomainException("OBSERVACIONES_INVALIDAS", "Las observaciones admiten hasta 1000 caracteres");
        }
        if ((estadoFisico != EstadoFisico.DISPONIBLE || accesoriosFaltantes) && observaciones.isBlank()) {
            throw new DomainException("OBSERVACIONES_REQUERIDAS",
                    "Describe los danos, la perdida o los accesorios faltantes");
        }
    }

    public EstadoFisico estadoResultante() {
        return accesoriosFaltantes && estadoFisico == EstadoFisico.DISPONIBLE
                ? EstadoFisico.EN_REVISION : estadoFisico;
    }
}
