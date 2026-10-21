# ADR-003 — Identificadores UUID tipados

- **Estado:** aceptado
- **Fecha:** 2026-10-20
- **Contexto:** el legacy usaba `UUID` en Java y `BIGINT` en PostgreSQL. Eso obligaba a
  conversiones frágiles y permitía pasar un ID de usuario donde se esperaba un ejemplar.

## Decisión

Usar `record` de Java 21 como identificadores tipados:

- `UsuarioId`
- `CatalogoItemId`
- `EjemplarId`
- `PrestamoId`
- `SancionId`

Cada uno envuelve un `UUID` generado en el dominio (`XxxId.nuevo()`), salvo los IDs que
provienen de una ruta o de un seed. Los repositorios y comandos usan estos tipos, no
`UUID` sueltos.

## Consecuencias

- El compilador impide mezclar identificadores.
- El dominio no depende de que la base de datos genere el ID.
- Los IDs son estables para seeds, demos y pruebas.
- Los DTOs REST siguen exponiendo `UUID` para el frontend; el mapeo ocurre en el borde.

## Alternativas descartadas

- **`Long` autogenerado:** acopla el dominio a la base y complica los seeds.
- **`UUID` sin envolver:** no aporta seguridad de tipos y fue justamente el problema del
  legacy.
