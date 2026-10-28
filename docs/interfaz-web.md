# Interfaz web y usabilidad

## 1. Objetivo

El frontend presenta una tarea clara por pantalla y consume la API del backend. Está construido con React, TypeScript y Vite.

## 2. Pantallas principales

| Pantalla | Función |
|---|---|
| Login | Iniciar sesión |
| Mi cuenta | Ver estado y completar correcciones |
| Catálogo | Buscar y filtrar títulos |
| Nueva reserva | Elegir horario y confirmar |
| Mis reservas | Consultar, cancelar o reprogramar |
| Mis préstamos | Consultar préstamos propios |
| Personas | Validar cuentas y conceder permisos |
| Aprobar reservas | Aprobar, denegar y entregar |
| Préstamos | Registrar préstamo, devolución e inspección |
| Catálogo administrativo | Crear títulos y ejemplares |
| Sanciones | Consultar y gestionar sanciones |

## 3. Navegación

React Router mantiene una URL por tarea. Los botones “Volver” y el historial del navegador permiten salir de un formulario sin perder orientación.

Ejemplos:

```text
/catalogo
/catalogo/{id}/reservar
/mis-reservas
/reservas
/prestamos/nuevo
/admin/catalogo
```

## 4. Principios de usabilidad

### Visibilidad

- El usuario ve el estado de cada reserva.
- La pantalla de reserva muestra anticipación, horario y duración.
- La administración ve solicitante, título, intervalo y plazo de recogida.

### Prevención de errores

- Los equipos muestran “En campus” sin permitir domicilio.
- Los libros muestran campus y domicilio.
- Los controles deshabilitan acciones no permitidas.
- El frontend valida fechas y el backend vuelve a validarlas.

### Consistencia

- “Reserva” significa solicitud futura.
- “Préstamo” significa entrega física.
- Estados y botones usan nombres iguales en todas las pantallas.

### Retroalimentación

- Los botones indican “Guardando”, “Enviando” o “Procesando”.
- Los errores de la API se traducen a mensajes claros.
- Las operaciones correctas muestran confirmación.

### Control del usuario

- Puede volver al catálogo.
- Puede cancelar una reserva activa.
- Puede reprogramar una solicitud dentro del plazo.
- Revisa los datos antes de confirmar.

### Accesibilidad

- Etiquetas asociadas a campos.
- Foco visible.
- Mensajes con `role="alert"` o `role="status"`.
- Contraste legible.
- Respeto por `prefers-reduced-motion`.

## 5. Cliente HTTP

`frontend/src/services/api.ts` centraliza las peticiones:

1. toma la URL de `VITE_API_BASE_URL`;
2. agrega el token de `localStorage`;
3. llama a `fetch`;
4. interpreta JSON;
5. transforma `ProblemDetail` en `ApiError`;
6. presenta un texto amigable según `codigo`.

La interfaz ayuda al usuario, pero el backend sigue siendo la fuente de verdad para permisos y reglas.

## 6. Construcción

```bash
cd frontend
pnpm install
pnpm typecheck
pnpm build
pnpm dev
```

Con npm:

```bash
npm install
npm run typecheck
npm run build
npm run dev
```
