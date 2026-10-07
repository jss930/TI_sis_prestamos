package com.universidad.biblioteca.dominio.persona;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad dentro del agregado Persona.
 * Define los privilegios de préstamo de la persona.
 */
public class Perfil {

    private final UUID idPerfil;
    private boolean prestamoDomicilioEquipos;
    private boolean plazoExtendidoLibros;

    /** Perfil nuevo: sin privilegios por defecto. */
    public Perfil(boolean prestamoDomicilioEquipos, boolean plazoExtendidoLibros) {
        this(UUID.randomUUID(), prestamoDomicilioEquipos, plazoExtendidoLibros);
    }

    /** Constructor completo, también lo usa el mapper al reconstruir desde la BD. */
    public Perfil(UUID idPerfil, boolean prestamoDomicilioEquipos, boolean plazoExtendidoLibros) {
        this.idPerfil = Objects.requireNonNull(idPerfil, "idPerfil es obligatorio");
        this.prestamoDomicilioEquipos = prestamoDomicilioEquipos;
        this.plazoExtendidoLibros = plazoExtendidoLibros;
    }

    /** Perfil estándar por tipo de persona (ajusta las reglas a tu negocio). */
    public static Perfil paraTipo(TipoPersona tipo) {
        return switch (tipo) {
            case ALUMNO         -> new Perfil(false, false);
            case DOCENTE        -> new Perfil(true, true);
            case ADMINISTRATIVO -> new Perfil(true, false);
        };
    }

    public void permitirPrestamoDomicilioEquipos(boolean permitido) {
        this.prestamoDomicilioEquipos = permitido;
    }

    public void permitirPlazoExtendidoLibros(boolean permitido) {
        this.plazoExtendidoLibros = permitido;
    }

    public UUID getIdPerfil() { return idPerfil; }
    public boolean tienePrestamoDomicilioEquipos() { return prestamoDomicilioEquipos; }
    public boolean tienePlazoExtendidoLibros() { return plazoExtendidoLibros; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Perfil otro && idPerfil.equals(otro.idPerfil));
    }

    @Override
    public int hashCode() {
        return idPerfil.hashCode();
    }
}