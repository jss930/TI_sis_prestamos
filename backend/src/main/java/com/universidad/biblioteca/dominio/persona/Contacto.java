package com.universidad.biblioteca.dominio.persona;

import java.util.Objects;

/**
 * Value Object: datos de contacto de una persona.
 * Es inmutable y se compara por valor.
 */
public final class Contacto {

    private final String correoInstitucional;
    private final String telefono;

    public Contacto(String correoInstitucional, String telefono) {
        this.correoInstitucional = validarCorreo(correoInstitucional);
        this.telefono = normalizarTelefono(telefono);
    }

    private static String validarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo institucional es obligatorio");
        }
        String limpio = correo.trim().toLowerCase();
        if (!limpio.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El correo institucional no tiene un formato válido: " + correo);
        }
        return limpio;
    }

    /** El teléfono es opcional: si viene vacío se guarda como null. */
    private static String normalizarTelefono(String telefono) {
        return (telefono == null || telefono.isBlank()) ? null : telefono.trim();
    }

    public String getCorreoInstitucional() { return correoInstitucional; }
    public String getTelefono() { return telefono; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contacto otro)) return false;
        return correoInstitucional.equals(otro.correoInstitucional)
                && Objects.equals(telefono, otro.telefono);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correoInstitucional, telefono);
    }

    @Override
    public String toString() {
        return correoInstitucional + (telefono != null ? " / " + telefono : "");
    }
}