package com.biblioteca.inventory.domain.model;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

/** Ubicacion fisica de un ejemplar. */
public record Ubicacion(String sala, String estante) implements ValueObject {

    public Ubicacion {
        sala = normalizar(sala);
        estante = normalizar(estante);
    }

    private static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "Sin asignar";
        }
        String limpio = valor.trim();
        if (limpio.length() > 50) {
            throw new DomainException("UBICACION_INVALIDA", "La ubicacion supera los 50 caracteres");
        }
        return limpio;
    }

    public static Ubicacion sinAsignar() {
        return new Ubicacion(null, null);
    }
}
