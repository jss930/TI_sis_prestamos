package com.biblioteca.identity.domain.model;

/**
 * Rol de acceso derivado de una cuenta. No es un perfil de prestamo: se calcula
 * a partir del estado y de los permisos operativos, en un unico lugar del dominio.
 */
public enum RolAcceso {
    ESTUDIANTE,
    DOCENTE,
    ADMINISTRATIVO,
    ADMINISTRADOR
}