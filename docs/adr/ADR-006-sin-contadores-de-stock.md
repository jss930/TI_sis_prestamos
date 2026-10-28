# ADR-006: Disponibilidad sin contadores de stock

**Estado:** aceptado.

## Decisión

`CatalogoItem` describe el título y no guarda stock. `Ejemplar` representa cada copia física. La disponibilidad se calcula desde los ejemplares, préstamos y reservas aprobadas.

## Consecuencias

- Existe una sola fuente de verdad.
- No puede desincronizarse un contador con las copias reales.
- Las consultas usan índices por título, copia, estado y horario.
