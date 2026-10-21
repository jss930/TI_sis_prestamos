# ADR-001 — Reescritura limpia en lugar de reparar el legacy

- **Estado:** aceptado
- **Fecha:** 2026-10-20
- **Contexto:** el repositorio original no compilaba (`Application` con nombre distinto al
  declarado, imports a `pe.edu.unsa.sisprestamos` inexistentes, `@EntityScan` roto,
  `UUID` en Java frente a `BIGINT` en SQL, repositorio incompleto). Además había dos
  fuentes de verdad para el inventario (`stockTotal`/`stockDisponible` en el catálogo y
  filas de ejemplares), seeds contradictorios y `target/` versionado.

## Decisión

Reescribir el backend desde cero sobre una rama `rewrite/v2`, conservando el estado
anterior en el tag `legacy-v0` y archivando el SQL en `docs/legacy/`. No se intentó
parchear archivo por archivo.

## Consecuencias

- El modelo se puede defender y testear desde el primer día.
- Se pierde la posibilidad de reutilizar controladores o entidades legacy, pero esos
  artefactos no eran funcionales.
- El historial queda claro: legacy identificable y rewrite aislado.
- El frontend legacy se conserva como prototipo, sin integrarlo todavía.

## Alternativas descartadas

- **Refactor incremental:** el punto de partida no compilaba; cada parche habría mezclado
  decisiones viejas y nuevas.
- **Microservicios desde el inicio:** sobredimensionado para un proyecto académico de un
  mes. Los contextos están separados en paquetes y pueden extraerse después.
