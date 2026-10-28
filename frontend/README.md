# Frontend del Sistema de Préstamos Universitario

Interfaz React + TypeScript + Vite que consume la API en `http://localhost:8080/api/v1`.

## Requisitos

- Node.js 20 recomendado.
- pnpm o npm.
- Backend y PostgreSQL para trabajar con datos reales.

## Ejecutar

```bash
cd frontend
pnpm install
pnpm dev
```

Con npm:

```bash
npm install
npm run dev
```

Abrir <http://localhost:5173>.

## Scripts

| Comando | Función |
|---|---|
| `pnpm dev` | Servidor de desarrollo |
| `pnpm typecheck` | Comprobar tipos TypeScript |
| `pnpm build` | Comprobar tipos y construir producción |
| `pnpm preview` | Servir el resultado de `dist/` |

## Configuración

`frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Estructura

```text
src/
├── App.tsx             sesión, datos y rutas
├── pages/              pantallas por tarea
├── components/         componentes reutilizables
├── services/api.ts     cliente HTTP
├── types.ts            contratos de la API
├── utils/              formato e imágenes
└── styles.css          diseño visual
```

## Flujo de datos

```text
Página React
  → api.ts
  → fetch + Bearer token
  → API Spring
  → JSON o ProblemDetail
  → estado React
  → mensaje o vista actualizada
```

## Pantallas

- Login y registro.
- Mi cuenta.
- Catálogo y reserva.
- Mis reservas y préstamos.
- Personas y permisos.
- Aprobación de reservas.
- Préstamos, devoluciones e inspecciones.
- Gestión de catálogo e inventario.
- Sanciones y administración.

## Reglas de interfaz

- Una tarea principal por pantalla.
- Botones de volver en formularios.
- Reglas visibles antes de confirmar.
- Equipos solo campus; libros con ambas opciones.
- Progreso, éxito y error visibles.
- Foco accesible y movimiento reducido.

## Seguridad

Después del login, el token se guarda localmente y `api.ts` lo envía como Bearer. La visibilidad de pantallas mejora la experiencia, pero las reglas y permisos siempre deben confirmarse en el backend.

## Errores

`api.ts` transforma `ProblemDetail` en `ApiError`. El campo `codigo` permite mostrar mensajes claros para horario, disponibilidad, sanciones, límites y permisos.
