# ADR-009 — Rol derivado de permisos y gestión de catálogo/usuarios

- **Estado:** aceptado
- **Fecha:** 2026-10-21
- **Contexto:** se completa el módulo de catálogo (alta, edición de título,
  activación/desactivación) y el de usuarios (alta provisional, validación,
  suspensión, reactivación, permisos). Hace falta definir qué es un
  "administrador" sin romper el modelo de perfiles de préstamo ni el aislamiento
  entre contextos.

## Decisión

1. **No se agrega `PerfilPrestatario.ADMINISTRADOR`.** El perfil (`ALUMNO`,
   `DOCENTE`, `ADMINISTRATIVO`) sigue definiendo únicamente la política de préstamo.
   `ADMINISTRATIVO` (perfil) no es lo mismo que `ADMINISTRADOR` (rol operativo).
2. **El rol de acceso se deriva en un único lugar del dominio**
   (`Usuario.rolAcceso()`), a partir del estado `VALIDADO` y de los permisos de
   gestión. Los permisos son la única fuente de verdad para autorizar.
3. **Se crea el permiso `GESTIONAR_USUARIOS`** para el alta, suspensión,
   reactivación y gestión de permisos. `VALIDAR_USUARIO_PROVISIONAL` queda solo
   para validar cuentas. Registrar y validar siguen siendo dos pasos separados.
4. **Activar/desactivar un ítem de catálogo es idempotente.** Repetir la
   operación no falla; solo hay efecto cuando hay transición real.
5. **Desactivar un ítem se permite siempre** y no consulta `inventory` ni
   `circulation` (preserva el aislamiento de contextos). Los ejemplares siguen
   consultables; un ítem desactivado deja de poder prestarse.
6. **Prohibido validar la propia cuenta y auto-otorgarse/revocarse permisos**
   (separación de funciones), con códigos de error estables.
7. **Sin autenticación real en esta fase:** `operadorId` sigue llegando en el body
   (y `DELETE` de permisos por query). Las escrituras no exigen permiso todavía.

## Consecuencias

- El frontend consume `rolAcceso` ya derivado y no recalcula el rol.
- El aislamiento de `catalog` e `identity` queda verificado por ArchUnit.
- La autorización real queda como limitación conocida hasta la fase de seguridad.

## Alternativas descartadas

- **Agregar `PerfilPrestatario.ADMINISTRADOR`:** mezclaría política de préstamo con
  rol operativo y requeriría cambios en persistencia y políticas.
- **Exigir permiso en endpoints sin `operadorId` ahora:** implicaría cambiar
  contratos de API sin autenticación, desaconsejado.