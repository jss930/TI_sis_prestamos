package com.biblioteca.identity.domain.model;

import java.util.Set;

/** Permisos operativos de ventanilla. No son roles: se otorgan explicitamente. */
public enum PermisoOperativo {
    ADMINISTRAR_SISTEMA,
    REGISTRAR_PRESTAMO,
    REGISTRAR_DEVOLUCION,
    VALIDAR_USUARIO_PROVISIONAL,
    GESTIONAR_CATALOGO,
    GESTIONAR_SANCIONES,
    GESTIONAR_USUARIOS;

    /**
     * Conjunto unico de permisos que convierten a una cuenta validada en
     * administrador del sistema. Cambiar esta lista es la unica forma de
     * redefinir que permisos otorgan el rol derivado {@code ADMINISTRADOR}.
     */
    public static final Set<PermisoOperativo> PERMISOS_DE_GESTION =
            Set.of(ADMINISTRAR_SISTEMA);
}
