# ADR-003: Identificadores UUID tipados

**Estado:** aceptado.

## Decisión

El dominio usa tipos como `UsuarioId`, `CatalogoItemId`, `EjemplarId`, `ReservaId`, `PrestamoId` y `SancionId`. Cada tipo envuelve un UUID.

## Consecuencias

- El compilador evita pasar un ID de usuario donde se espera una copia.
- Los UUID pueden generarse en la aplicación.
- Los DTO HTTP exponen UUID y los controladores los convierten al tipo correcto.
