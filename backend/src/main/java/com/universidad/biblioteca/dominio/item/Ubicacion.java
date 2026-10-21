package com.universidad.biblioteca.dominio.item;

import java.util.Objects;

/**
 * Value Object: ubicación física de un ítem.
 * Es inmutable y se compara por valor, no por identidad.
 */
public final class Ubicacion {

    private final String edificio;
    private final String sala;
    private final String estante;

    public Ubicacion(String edificio, String sala, String estante) {
        this.edificio = requerido(edificio, "edificio");
        this.sala = requerido(sala, "sala");
        this.estante = requerido(estante, "estante");
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio");
        }
        return valor.trim();
    }

    public String getEdificio() { return edificio; }
    public String getSala() { return sala; }
    public String getEstante() { return estante; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ubicacion otra)) return false;
        return edificio.equals(otra.edificio)
                && sala.equals(otra.sala)
                && estante.equals(otra.estante);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edificio, sala, estante);
    }

    @Override
    public String toString() {
        return edificio + " / " + sala + " / " + estante;
    }
}