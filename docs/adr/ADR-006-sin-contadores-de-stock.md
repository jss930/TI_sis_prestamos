# ADR-006 — Sin contadores de stock en el catálogo

- **Estado:** aceptado
- **Fecha:** 2026-10-20
- **Contexto:** el legacy guardaba `stockTotal` y `stockDisponible` en el ítem de catálogo
  y, al mismo tiempo, tenía filas de ejemplares. Los valores se desincronizaban: se podía
  prestar un ejemplar ya prestado o mostrar stock disponible inexistente. No había una
  restricción de “un préstamo activo por ejemplar”.

## Decisión

- `CatalogoItem` describe la obra o equipo y **no tiene contadores de stock**.
- `Ejemplar` es la unidad física prestable, con `Barcode` único y estado físico.
- La disponibilidad se deriva de `Ejemplar.estaDisponible()` y de la ausencia de un
  préstamo activo para ese ejemplar.
- La regla “un préstamo activo por ejemplar” se valida en `RegistrarPrestamoUseCase` y,
  en la fase 2, se refuerza con un índice único parcial en PostgreSQL.

## Consecuencias

- Una sola fuente de verdad: el ejemplar y su préstamo.
- El catálogo puede mostrar “N ejemplares” calculando sobre inventario; no se almacena.
- Las consultas de disponibilidad requieren agregar ejemplares y préstamos; es un costo
  aceptable y se puede optimizar con una vista/materialized view si hiciera falta.
- Se elimina la clase de bugs de doble contabilidad.

## Alternativas descartadas

- **Mantener contadores y ejemplares:** obliga a transacciones coordinadas y sigue siendo
  fácil de desincronizar.
- **Solo contadores, sin ejemplares:** impide saber qué unidad física se prestó, que es un
  requisito del negocio (barcode, estado, ubicación, pérdida).
