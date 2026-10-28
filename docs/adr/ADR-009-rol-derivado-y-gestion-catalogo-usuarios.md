# ADR-009: Administrador derivado y permisos por cuenta

**Estado:** aceptado.

## Decisión

Los perfiles de préstamo son `ALUMNO`, `DOCENTE` y `ADMINISTRATIVO`. Una cuenta validada se considera administradora cuando posee `ADMINISTRAR_SISTEMA`.

Los permisos operativos se conceden directamente a cada cuenta. El rol de acceso se calcula en `Usuario.rolAcceso()`.

## Consecuencias

- Perfil y permiso no se confunden.
- La política de préstamo depende del perfil.
- La navegación administrativa depende de permisos.
- No se permite autovalidación ni autoasignación de permisos.
