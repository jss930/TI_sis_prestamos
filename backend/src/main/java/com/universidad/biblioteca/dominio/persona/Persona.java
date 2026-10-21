package com.universidad.biblioteca.dominio.persona;

import java.util.Objects;
import java.util.UUID;

/**
 * Raíz del agregado Persona.
 * Alumno, Docente y Administrativo la extienden; /tipoPersona es derivado
 * (lo determina la subclase).
 */
public abstract class Persona {

    private final UUID idPersona;
    private String nombreCompleto;
    private EstadoCuenta estadoCuenta;
    private boolean esAdminSistema;
    private Contacto contacto;
    private Perfil perfil;

    /** Constructor para una persona nueva: la cuenta nace ACTIVA. */
    protected Persona(String nombreCompleto, Contacto contacto, Perfil perfil) {
        this(UUID.randomUUID(), nombreCompleto, EstadoCuenta.ACTIVA, false, contacto, perfil);
    }

    /** Constructor completo, también lo usa el mapper al reconstruir desde la BD. */
    protected Persona(UUID idPersona, String nombreCompleto, EstadoCuenta estadoCuenta,
                      boolean esAdminSistema, Contacto contacto, Perfil perfil) {
        this.idPersona = Objects.requireNonNull(idPersona, "idPersona es obligatorio");
        this.nombreCompleto = validarNombre(nombreCompleto);
        this.estadoCuenta = Objects.requireNonNull(estadoCuenta, "estadoCuenta es obligatorio");
        this.esAdminSistema = esAdminSistema;
        this.contacto = Objects.requireNonNull(contacto, "contacto es obligatorio");
        this.perfil = Objects.requireNonNull(perfil, "perfil es obligatorio");
    }

    /** Una persona solo puede solicitar préstamos si su cuenta está ACTIVA. */
    public boolean puedeSolicitar() {
        return estadoCuenta == EstadoCuenta.ACTIVA;
    }

    public void cambiarEstadoCuenta(EstadoCuenta nuevoEstado) {
        Objects.requireNonNull(nuevoEstado, "El nuevo estado es obligatorio");
        this.estadoCuenta = nuevoEstado;
    }

    public abstract TipoPersona getTipoPersona();

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        return nombre.trim();
    }

    public UUID getIdPersona() { return idPersona; }
    public String getNombreCompleto() { return nombreCompleto; }
    public EstadoCuenta getEstadoCuenta() { return estadoCuenta; }
    public boolean esAdminSistema() { return esAdminSistema; }
    public Contacto getContacto() { return contacto; }
    public Perfil getPerfil() { return perfil; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Persona otra && idPersona.equals(otra.idPersona));
    }

    @Override
    public int hashCode() {
        return idPersona.hashCode();
    }
}