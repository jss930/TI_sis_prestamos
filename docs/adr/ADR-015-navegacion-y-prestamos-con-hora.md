# ADR-015: Navegación por tareas y precisión horaria

**Estado:** aceptado.

## Decisión

El frontend usa una ruta por tarea y botones de regreso. El diseño aplica jerarquía visual, contraste, foco visible y reducción de movimiento.

Reservas y préstamos guardan hora exacta de inicio, vencimiento y devolución. La entrega presencial no puede registrarse en el futuro y el vencimiento debe ser posterior.

## Consecuencias

- Cada pantalla tiene una acción principal.
- Catálogo público y gestión administrativa están separados.
- Las URLs pueden compartirse y el historial funciona.
- La base conserva precisión horaria.
