# Frontend — React + TypeScript + Vite

Aplicación web del sistema de préstamos. Consume la API REST del backend en
`http://localhost:8080/api/v1`.

## Requisitos

- **Node.js 18 o superior** (recomendado Node 20).
- **pnpm** (recomendado) o `npm`.
- Backend y PostgreSQL en marcha para ver datos reales.

Verifica:

```bash
node --version
pnpm --version   # o: npm --version
```

## Ejecutar

### Linux / macOS

```bash
cd frontend
./iniciar.sh
```

O manualmente:

```bash
cd frontend
pnpm install
pnpm dev
```

### Windows

```bat
cd frontend
iniciar.bat
```

O manualmente en PowerShell/CMD:

```powershell
cd frontend
pnpm install
pnpm dev
```

Vite sirve la aplicación en <http://localhost:5173>.

## Configuración

El archivo `.env` contiene:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

`.env` está ignorado por Git. Usa `.env.example` como plantilla:

**Linux / macOS**

```bash
cp .env.example .env
```

**Windows CMD**

```bat
copy .env.example .env
```

**Windows PowerShell**

```powershell
Copy-Item .env.example .env
```

Si cambias el puerto del backend, actualiza también el origen permitido en
`backend/.../infrastructure/config/WebConfig.java` (`http://localhost:5173` por defecto).

## Endpoints consumidos

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/catalogo` | Catálogo de libros, equipos y otros |
| `GET` | `/usuarios` | Personas, estados y permisos |
| `GET` | `/inventario/ejemplares` | Unidades físicas y disponibilidad |
| `GET` | `/prestamos` | Historial de préstamos |
| `POST` | `/prestamos` | Registrar un préstamo |
| `POST` | `/prestamos/{id}/devolucion` | Registrar una devolución |
| `GET` | `/sanciones` | Sanciones y vigencia |

`POST /prestamos` requiere `operadorId`, `prestatarioId`, `ejemplarId` y `lugarUso`
(`EN_CAMPUS` o `DOMICILIO`). El frontend obtiene los ejemplares del endpoint de inventario
porque el catálogo no incluye unidades físicas.

## Errores `ProblemDetail`

`src/services/api.ts` centraliza `fetch` y convierte las respuestas
`application/problem+json` en `ApiError`. El mensaje se elige por el campo `codigo`, con
textos amigables para:

- `SANCION_ACTIVA`
- `USUARIO_NO_HABILITADO`
- `AUTOPRESTAMO_NO_PERMITIDO`
- `EJEMPLAR_NO_DISPONIBLE`
- `PRESTAMO_VENCIDO_PENDIENTE`
- `LIMITE_PRESTAMOS`
- `DOMICILIO_NO_PERMITIDO`
- `OPERADOR_NO_HABILITADO`
- `VALIDACION`

Si la API no responde, se muestra `SIN_CONEXION` con una indicación de revisar el backend.

## Estructura

```text
src/
├── App.tsx                 # Sesión demo, carga de datos y navegación
├── main.tsx
├── types.ts                # DTOs alineados con la API
├── constants.ts            # Operador demo
├── services/api.ts         # Cliente HTTP y ApiError
├── utils/format.ts         # Fechas, moneda y nombres
├── components/             # Barra lateral, encabezado, tabla y etiquetas
└── pages/                  # Login, resumen, personas, catálogo, préstamos, sanciones
```

## Scripts

```bash
pnpm dev         # servidor de desarrollo
pnpm typecheck   # TypeScript en modo estricto
pnpm build       # typecheck + build de producción
pnpm preview     # sirve el build de dist/
```

## Autenticación

La autenticación real todavía no está implementada. El login es una demostración local:
no valida credenciales ni protege la API. El catálogo puede consultarse sin sesión; las
operaciones de ventanilla se muestran tras el login demo. La fase de seguridad reemplazará
este comportamiento por JWT y roles.

## Alcance

El frontend no incluye reservas: el backend no expone ese módulo todavía. Las reglas de
negocio (disponibilidad, sanciones, límites y plazos) se aplican siempre en el backend; la
interfaz solo muestra los mensajes de error.
