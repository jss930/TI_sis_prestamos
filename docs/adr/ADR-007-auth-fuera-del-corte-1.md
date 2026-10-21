# ADR-007 — Autenticación y autorización fuera del corte 1

- **Estado:** aceptado (decisión temporal)
- **Fecha:** 2026-10-20
- **Contexto:** el corte 1 debía demostrar el flujo de préstamos y sanciones hoy. Montar
  JWT, usuarios de seguridad, refresh tokens y integración con el frontend habría
  consumido el tiempo disponible y no era necesario para validar el dominio.

## Decisión

En este corte, los endpoints reciben `operadorId` en el body o como query param y confían
en él. Los permisos se validan contra la cuenta indicada (`puedeOperarVentanilla`,
`puedeGestionarSanciones`, etc.), pero **no hay autenticación real**.

Los endpoints de catálogo e inventario todavía no exigen `GESTIONAR_CATALOGO`.

## Consecuencias

- La demo es simple y reproducible con `curl`.
- No se debe exponer esta API a una red no confiable.
- La fase 3 introduce Spring Security + JWT:
  - login de personal y, si se confirma el alcance, de alumnos;
  - el operador se deriva del token, no del body;
  - roles/permisos mapeados a `PermisoOperativo`;
  - los endpoints de catálogo e inventario exigen `GESTIONAR_CATALOGO`.
- Los casos de uso ya reciben `operadorId`, así que el cambio se concentra en el borde
  (filtro JWT + controladores).

## Alternativas descartadas

- **Basic Auth hardcodeado:** da falsa sensación de seguridad y ensucia la demo.
- **JWT ahora:** retrasa el objetivo del corte y mezcla dominio con seguridad antes de
  validar las reglas de negocio.
