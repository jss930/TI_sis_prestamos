package com.biblioteca.identity.domain.model;

/** Permisos operativos de ventanilla. No son roles: se otorgan explicitamente. */
public enum PermisoOperativo {
    REGISTRAR_PRESTAMO,
    VALIDAR_USUARIO_PROVISIONAL,
    GESTIONAR_CATALOGO,
    GESTIONAR_SANCIONES
}
