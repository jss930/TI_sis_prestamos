# ADR-015: Navegación por tareas y préstamos con precisión horaria

Estado: aceptado.

## Decisión

El frontend usa React Router y una ruta por tarea. Consultar catálogo, reservar, gestionar títulos, registrar ejemplares y registrar préstamos dejan de compartir formularios en una sola pantalla. El historial del navegador y los botones «Volver» permiten abandonar una tarea sin perder orientación.

La identidad visual usa Soft UI azul/verde: sombras suaves solo para expresar jerarquía, controles con contraste suficiente y foco visible. Se evita el neumorfismo puro porque su bajo contraste perjudica accesibilidad.

Los préstamos conservan las fechas históricas y añaden `fecha_hora_entrega`, `fecha_hora_vencimiento` y `fecha_hora_devolucion` mediante Flyway V14. La entrega presencial no puede ser futura, el vencimiento debe ser posterior y no puede superar el plazo de la política.

## Consecuencias

- Una pantalla tiene una tarea y una acción primaria.
- El catálogo de consulta queda separado de su administración.
- La selección de personas usa búsqueda limitada en vez de un selector enorme.
- Las URLs se pueden enlazar y el botón atrás funciona.
- La precisión horaria no rompe las columnas de fecha existentes ni sus datos históricos.
