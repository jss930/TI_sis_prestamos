<div align="center">

# Demo reproducible con `curl`

### Biblioteca UNSA — Recorrido funcional de extremo a extremo

![Demo](https://img.shields.io/badge/Demo-curl-4E9A06?style=for-the-badge&logo=curl&logoColor=white)
![API](https://img.shields.io/badge/API-REST-0B7285?style=for-the-badge)
![Perfil](https://img.shields.io/badge/Perfil-demo-6A5ACD?style=for-the-badge)

**Guía para sustentación y verificación manual**

</div>

> [!NOTE]
> Esta demo usa el perfil `demo`, que carga datos con IDs fijos y un préstamo vencido sobre
> PostgreSQL. Flyway crea el esquema al arrancar y el seed solo se ejecuta si la tabla
> `usuarios` está vacía. Los datos **persisten** entre reinicios; para volver al escenario
> inicial hay que borrar el volumen de PostgreSQL.

---

## 0. Antes de empezar

### 0.1 Requisitos

| Herramienta | Versión | Uso |
|---|---|---|
| JDK | 21 | Backend |
| Maven | 3.9+ o wrapper incluido | Compilación y ejecución |
| Docker + Compose | Actual | PostgreSQL 16 y Adminer |
| `curl` | Actual | Peticiones HTTP de la demo |
| `python3` | 3.x | Extraer IDs de respuestas JSON |
| Node.js + pnpm | 18+ / pnpm 9+ | Frontend integrado |

> [!IMPORTANT]
> La demo completa requiere **PostgreSQL**. Los tests del backend no lo requieren porque
> usan H2 en modo PostgreSQL, pero la aplicación en ejecución sí necesita la base de datos
> levantada con Docker Compose.

### 0.2 Arranque por sistema operativo

**Linux / macOS**

```bash
# Desde la raíz del repositorio
docker compose up -d

# En otra terminal
cd backend
./mvnw spring-boot:run
```

**Windows PowerShell**

```powershell
# Desde la raíz del repositorio
docker compose up -d

# En otra terminal
cd backend

# Si JAVA_HOME no está configurado:
$env:JAVA_HOME = "C:\ruta\a\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Con Maven instalado:
mvn spring-boot:run

# Alternativa sin Maven global:
.\mvnw.cmd spring-boot:run
```

**Windows CMD**

```bat
docker compose up -d
cd backend
set "JAVA_HOME=C:\ruta\a\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
mvn spring-boot:run
```

| Acción | Linux / macOS | Windows PowerShell | Windows CMD |
|---|---|---|---|
| Levantar PostgreSQL | `docker compose up -d` | `docker compose up -d` | `docker compose up -d` |
| Arrancar API | `./mvnw spring-boot:run` | `mvn spring-boot:run` | `mvn spring-boot:run` |
| Alternativa con wrapper | `./mvnw spring-boot:run` | `.\mvnw.cmd spring-boot:run` | `mvnw.cmd spring-boot:run` |
| Ejecutar tests | `./mvnw test` | `mvn test` | `mvn test` |

Esto levanta:

| Servicio | URL | Credenciales |
|---|---|---|
| PostgreSQL | `localhost:5432` | base/usuario/clave: `biblioteca` |
| Adminer | <http://localhost:8081> | Gestión visual de la base |
| API | <http://localhost:8080/api/v1> | — |
| Health | <http://localhost:8080/actuator/health> | — |
| Frontend | <http://localhost:5173> | Login demo |

### 0.3 Variables de la demo

En **Linux/macOS** o **Git Bash/WSL**:

```bash
export BASE=http://localhost:8080/api/v1
curl -s http://localhost:8080/actuator/health
# {"status":"UP"}
```

En **Windows PowerShell**:

```powershell
$BASE = "http://localhost:8080/api/v1"
curl.exe -s http://localhost:8080/actuator/health
curl.exe -s "$BASE/catalogo" | ConvertFrom-Json | ConvertTo-Json -Depth 5
```

> [!TIP]
> `curl.exe` viene incluido en Windows 10/11. En PowerShell, `curl` sin `.exe` es un alias
> de `Invoke-WebRequest` y no acepta los mismos parámetros. Si usas Git Bash o WSL, puedes
> ejecutar los ejemplos bash tal cual.

### 0.4 IDs fijos del escenario

```bash
BIBLIOTECARIO=00000000-0000-0000-0000-000000000001
ANA=00000000-0000-0000-0000-000000000002
LUIS=00000000-0000-0000-0000-000000000003
DOCENTE=00000000-0000-0000-0000-000000000004
LIBRO=00000000-0000-0000-0000-000000000101
LAPTOP=00000000-0000-0000-0000-000000000102
LIB001=00000000-0000-0000-0000-000000000201
LIB002=00000000-0000-0000-0000-000000000202
LAP001=00000000-0000-0000-0000-000000000203
PRESTAMO_VENCIDO=00000000-0000-0000-0000-000000000301
```

| Recurso | UUID | Estado / nota |
|---|---|---|
| Bibliotecario (Elena) | `00000000-0000-0000-0000-000000000001` | `VALIDADO`, todos los permisos |
| Ana (alumna) | `00000000-0000-0000-0000-000000000002` | `PROVISIONAL`, no puede prestar |
| Luis (alumno) | `00000000-0000-0000-0000-000000000003` | `VALIDADO`, préstamo vencido |
| Rosa (docente) | `00000000-0000-0000-0000-000000000004` | `VALIDADO`, equipos a domicilio |
| Libro *Clean Code* | `00000000-0000-0000-0000-000000000101` | ISBN `9780132350884` |
| Laptop Lenovo T14 | `00000000-0000-0000-0000-000000000102` | Equipo |
| Ejemplar `LIB-001` | `00000000-0000-0000-0000-000000000201` | Disponible |
| Ejemplar `LIB-002` | `00000000-0000-0000-0000-000000000202` | Prestado a Luis, vencido |
| Ejemplar `LAP-001` | `00000000-0000-0000-0000-000000000203` | Disponible |
| Préstamo vencido | `00000000-0000-0000-0000-000000000301` | Vence 3 días antes de “hoy” |

---

## 1. Catálogo e inventario

```bash
curl -s "$BASE/catalogo"
curl -s "$BASE/inventario/ejemplares"
```

**Resultado esperado:**

- *Clean Code* (`LIBRO`) con ISBN `9780132350884`.
- *Laptop Lenovo ThinkPad T14* (`EQUIPO`).
- `LIB-001` disponible, `LIB-002` prestado, `LAP-001` disponible.

La disponibilidad no sale de un contador: sale del `estadoFisico` de cada ejemplar.

### Gestión del catálogo (alta, título y activación)

```bash
# Alta de libro
curl -s -i -X POST "$BASE/catalogo/libros" \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Refactoring","isbn":"9780201485677","autor":"Martin Fowler","editorial":"Addison-Wesley","anioPublicacion":1999}'

# Cambio de titulo (idempotente en activar/desactivar)
ITEM_ID=00000000-0000-0000-0000-000000000101
curl -s -i -X PATCH "$BASE/catalogo/$ITEM_ID/titulo" \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Clean Code (edicion revisada)"}'
curl -s -i -X POST "$BASE/catalogo/$ITEM_ID/desactivar"
curl -s -i -X POST "$BASE/catalogo/$ITEM_ID/activar"
```

**Resultado esperado:** 200/201 con el ítem actualizado; repetir activar o desactivar no falla.

> [!NOTE]
> Esta separación es intencional. `catalog` describe la obra; `inventory` describe la
> unidad física. Un mismo libro puede tener varios ejemplares con estados distintos.

---

## 2. Una cuenta `PROVISIONAL` no puede prestar

Ana existe, pero su cuenta aún no fue validada.

```bash
curl -s -i -X POST "$BASE/prestamos" \
  -H 'Content-Type: application/json' \
  -d "{
    \"operadorId\": \"$BIBLIOTECARIO\",
    \"prestatarioId\": \"$ANA\",
    \"ejemplarId\": \"$LIB001\",
    \"lugarUso\": \"EN_CAMPUS\"
  }"
```

**Resultado esperado: HTTP 409**

```json
{
  "title": "Regla de negocio",
  "status": 409,
  "detail": "La cuenta PROVISIONAL no puede solicitar prestamos",
  "codigo": "USUARIO_NO_HABILITADO"
}
```

Ana sí puede consultar el catálogo:

```bash
curl -s "$BASE/catalogo"
```

> [!IMPORTANT]
> `PROVISIONAL` no significa “sin cuenta”: significa que la cuenta existe, pero todavía no
> fue validada. Puede consultar el catálogo; no puede solicitar préstamos ni operar
> ventanilla.

---

## 3. Devolución tardía → sanción automática

Luis tiene el préstamo `...0301`, vencido hace 3 días.

```bash
curl -s -i -X POST "$BASE/prestamos/$PRESTAMO_VENCIDO/devolucion" \
  -H 'Content-Type: application/json' \
  -d '{"estadoFisico":"DISPONIBLE"}'
```

**Resultado esperado: HTTP 200**

```json
{
  "id": "00000000-0000-0000-0000-000000000301",
  "estado": "DEVUELTO",
  "fechaDevolucion": "2026-10-20",
  "vencido": false,
  "diasRetraso": 3
}
```

> [!NOTE]
> La fecha exacta depende del día en que ejecutes la demo, pero `diasRetraso` siempre es
> **3** porque el seed calcula el vencimiento como “hoy menos 3 días”.

El evento `PrestamoDevueltoConRetrasoEvent` fue consumido por `penalties`:

```bash
curl -s "$BASE/sanciones?usuarioId=$LUIS"
```

**Resultado esperado:** una sanción `ACTIVA` por `RETRASO`, `diasSuspension: 3` y
`multa: 3.00`.

---

## 4. La sanción bloquea el siguiente préstamo

```bash
curl -s -i -X POST "$BASE/prestamos" \
  -H 'Content-Type: application/json' \
  -d "{
    \"operadorId\": \"$BIBLIOTECARIO\",
    \"prestatarioId\": \"$LUIS\",
    \"ejemplarId\": \"$LIB001\",
    \"lugarUso\": \"EN_CAMPUS\"
  }"
```

**Resultado esperado: HTTP 409**

```json
{
  "title": "Regla de negocio",
  "status": 409,
  "detail": "El prestatario tiene una sancion activa",
  "codigo": "SANCION_ACTIVA"
}
```

> [!TIP]
> Este es el punto ideal para explicar el desacoplamiento: la **creación** de la sanción no
> se invoca directamente. `circulation` publica un evento y `penalties` reacciona. Para
> bloquear el préstamo, `circulation` solo consulta el puerto de lectura
> `SancionRepository.existeActivaPara`; no conoce el agregado `Sancion`.

---

## 5. Autopréstamo prohibido

```bash
curl -s -i -X POST "$BASE/prestamos" \
  -H 'Content-Type: application/json' \
  -d "{
    \"operadorId\": \"$BIBLIOTECARIO\",
    \"prestatarioId\": \"$BIBLIOTECARIO\",
    \"ejemplarId\": \"$LIB001\",
    \"lugarUso\": \"EN_CAMPUS\"
  }"
```

**Resultado esperado: HTTP 409**, código `AUTOPRESTAMO_NO_PERMITIDO`.

La invariante vive en `Prestamo`: el operador que registra el préstamo no puede ser la
misma persona que lo recibe.

---

## 6. Docente lleva un equipo a domicilio

```bash
curl -s -i -X POST "$BASE/prestamos" \
  -H 'Content-Type: application/json' \
  -d "{
    \"operadorId\": \"$BIBLIOTECARIO\",
    \"prestatarioId\": \"$DOCENTE\",
    \"ejemplarId\": \"$LAP001\",
    \"lugarUso\": \"DOMICILIO\"
  }"
```

**Resultado esperado: HTTP 201**, `estado: ACTIVO`, vencimiento a 7 días (política de
docente/equipo).

Si repites la operación con `$ANA` o `$LUIS` (alumnos) y un equipo a domicilio, obtendrás
`DOMICILIO_NO_PERMITIDO`.

> [!NOTE]
> La política de préstamo combina **perfil**, **tipo de recurso** y **lugar de uso**. No es
> una regla hardcodeada en el controlador; vive en `PoliticaPrestamoEstandar`.

---

## 7. Cumplir la sanción y volver a prestar

Obtén el ID de la sanción y cúmplela:

```bash
SANCION=$(curl -s "$BASE/sanciones?usuarioId=$LUIS" \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)[0]["id"])')

curl -s -i -X POST "$BASE/sanciones/$SANCION/cumplir" \
  -H 'Content-Type: application/json' \
  -d "{\"operadorId\":\"$BIBLIOTECARIO\"}"
```

**Resultado esperado: HTTP 200**, `estado: CUMPLIDA`.

Ahora el mismo préstamo que antes fallaba debe funcionar:

```bash
curl -s -i -X POST "$BASE/prestamos" \
  -H 'Content-Type: application/json' \
  -d "{
    \"operadorId\": \"$BIBLIOTECARIO\",
    \"prestatarioId\": \"$LUIS\",
    \"ejemplarId\": \"$LIB001\",
    \"lugarUso\": \"EN_CAMPUS\"
  }"
```

**Resultado esperado: HTTP 201**, `estado: ACTIVO`, vencimiento a 7 días (alumno/libro).

---

## 8. Renovar un préstamo activo

Toma el ID del préstamo recién creado:

```bash
PRESTAMO=$(curl -s "$BASE/prestamos?prestatarioId=$LUIS&estado=ACTIVO" \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)[0]["id"])')

curl -s -i -X POST "$BASE/prestamos/$PRESTAMO/renovacion"
```

**Resultado esperado: HTTP 200**, `renovaciones: 1` y vencimiento extendido 7 días.

Una segunda renovación para un alumno debe fallar con `LIMITE_RENOVACIONES`.

> [!NOTE]
> El límite de renovaciones depende del perfil: 1 para alumnos y 2 para docentes y
> administrativos. La validación se apoya en `PoliticaPrestamo`.

---

## 9. Alta y validación de una cuenta

```bash
NUEVO=$(curl -s -X POST "$BASE/usuarios" \
  -H 'Content-Type: application/json' \
  -d '{"email":"carla@universidad.edu.pe","nombreCompleto":"Carla Nueva","perfil":"ALUMNO"}')

echo "$NUEVO"
CARLA=$(echo "$NUEVO" | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')
```

**Resultado esperado: HTTP 201**, `estado: PROVISIONAL`, `permisos: []`.

```bash
curl -s -X POST "$BASE/usuarios/$CARLA/validar" \
  -H 'Content-Type: application/json' \
  -d "{\"operadorId\":\"$BIBLIOTECARIO\"}"
```

**Resultado esperado:** `estado: VALIDADO` y `fechaValidacion` no nula.

```bash
curl -s -X POST "$BASE/usuarios/$CARLA/permisos" \
  -H 'Content-Type: application/json' \
  -d "{\"operadorId\":\"$BIBLIOTECARIO\",\"permiso\":\"REGISTRAR_PRESTAMO\"}"
```

**Resultado esperado:** `permisos: ["REGISTRAR_PRESTAMO"]`. Ahora Carla puede operar
ventanilla.

> [!IMPORTANT]
> Un permiso operativo **no** salta la validación de la cuenta. Primero se valida; después
> se otorgan permisos. Esta separación evita que un permiso aislado convierta una cuenta
> provisional en operativa.

---

## 10. Sanción manual

```bash
curl -s -X POST "$BASE/sanciones" \
  -H 'Content-Type: application/json' \
  -d "{
    \"operadorId\": \"$BIBLIOTECARIO\",
    \"usuarioId\": \"$CARLA\",
    \"motivo\": \"DANO\",
    \"diasSuspension\": 2,
    \"multa\": 15.00,
    \"observaciones\": \"Tapa rota\"
  }"
```

**Resultado esperado: HTTP 201**, `estado: ACTIVA`, `fechaFin` dos días después de
`fechaInicio`.

---

## 11. Errores útiles para el frontend

Todos los errores son `ProblemDetail` (`application/problem+json`) e incluyen un campo
`codigo` estable:

| Código | HTTP | Significado |
|---|---:|---|
| `USUARIO_NO_HABILITADO` | 409 | Cuenta no validada |
| `USUARIO_NO_VALIDADO` | 409 | Se intentó otorgar un permiso a una cuenta no validada |
| `OPERADOR_NO_HABILITADO` | 409 | Operador sin permiso |
| `AUTOPRESTAMO_NO_PERMITIDO` | 409 | Operador = prestatario |
| `EMAIL_DUPLICADO` | 409 | Ya existe una cuenta con ese correo |
| `ITEM_INACTIVO` | 409 | El ítem de catálogo está fuera de circulación |
| `EJEMPLAR_NO_DISPONIBLE` | 409 | Ejemplar prestado, dañado, perdido o de baja |
| `EJEMPLAR_YA_PRESTADO` | 409 | Ya existe un préstamo activo sobre el ejemplar |
| `EJEMPLAR_NO_PRESTADO` | 409 | Se intentó devolver un ejemplar sin préstamo activo |
| `EJEMPLAR_PRESTADO` | 409 | No se puede dar de baja un ejemplar prestado |
| `BARCODE_DUPLICADO` | 409 | Ya existe un ejemplar con ese código de barras |
| `ISBN_DUPLICADO` | 409 | Ya existe un libro con ese ISBN |
| `SANCION_ACTIVA` | 409 | Sanción vigente |
| `SANCION_NO_ACTIVA` | 409 | La sanción ya fue cumplida o anulada |
| `PRESTAMO_VENCIDO_PENDIENTE` | 409 | Debe devolver antes de pedir otro |
| `PRESTAMO_VENCIDO` | 409 | No se puede renovar un préstamo vencido |
| `PRESTAMO_NO_ACTIVO` | 409 | Devolución/renovación sobre préstamo cerrado |
| `LIMITE_PRESTAMOS` | 409 | Máximo simultáneo alcanzado |
| `LIMITE_RENOVACIONES` | 409 | Sin renovaciones disponibles |
| `DOMICILIO_NO_PERMITIDO` | 409 | Equipo a domicilio para alumno |
| `TRANSICION_INVALIDA` | 409 | Transición de estado no permitida |
| `VALIDACION` | 400 | Campos inválidos (`campos` en el body) |
| `PETICION_INVALIDA` | 400 | JSON o parámetro mal formado |
| `ERROR_INTERNO` | 500 | Error no controlado |

> [!TIP]
> El frontend traduce `codigo` a mensajes amigables. Si agregas una regla de negocio,
> define un código nuevo y documéntalo aquí y en el manejador global de errores.

---

## 12. Reiniciar la demo

```bash
# Ctrl+C en la terminal de Spring Boot
docker compose down -v
docker compose up -d
cd backend
./mvnw spring-boot:run
```

En Windows:

```powershell
docker compose down -v
docker compose up -d
cd backend
mvn spring-boot:run
```

`docker compose down -v` borra el volumen `biblioteca_pgdata`. Al arrancar de nuevo,
Flyway aplica la migración y el perfil `demo` vuelve a dejar el préstamo vencido y sin
sanciones.

> [!WARNING]
> No uses `docker compose down -v` si necesitas conservar datos. El flag `-v` elimina
> también el volumen de PostgreSQL.

---

## 13. Probar el frontend integrado

Con PostgreSQL y el backend en marcha:

```bash
cd frontend
pnpm install
pnpm dev
```

Alternativa con npm:

```bash
npm install
npm run dev
```

Abre <http://localhost:5173>. El catálogo es visible sin sesión mediante **Ver catálogo
público**. Para operar, usa el login demo (cualquier correo y contraseña; no hay
autenticación real todavía).

**Flujo sugerido:**

1. Entra con una cuenta demo.
2. Revisa **Resumen**, **Personas** y **Catálogo**.
3. En **Préstamos**, registra un préstamo para un usuario `VALIDADO` y un ejemplar
   `DISPONIBLE`.
4. Devuelve el préstamo vencido de Luis y observa la sanción generada en **Sanciones**.
5. Intenta otro préstamo para Luis: el frontend muestra el mensaje amigable de
   `SANCION_ACTIVA` usando el `codigo` de `application/problem+json`.

El frontend usa:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Si cambias el puerto del backend, actualiza `frontend/.env` y la configuración CORS de
`WebConfig`.

> [!NOTE]
> El login del frontend es **solo demostración**. No hay JWT ni autorización real; los
> `operadorId` se envían en el cuerpo de las peticiones. No uses credenciales reales ni
> expongas la API a Internet en este estado.

---

## 14. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Connection refused` al arrancar la API | PostgreSQL no está levantado | `docker compose up -d` y esperar el healthcheck |
| `Port 5432 is already in use` | Otro PostgreSQL local ocupa el puerto | Detener el servicio local o cambiar el puerto en `docker-compose.yml` |
| `Port 8080 is already in use` | Otra instancia del backend | Detener el proceso anterior o cambiar `server.port` |
| `401`/`403` inesperado | La seguridad real aún no existe | Revisar que el login demo no se esté usando como autenticación |
| Error de CORS en el navegador | Origen del frontend no permitido | Revisar `WebConfig` y `VITE_API_BASE_URL` |
| La demo no vuelve al estado inicial | El volumen persiste | `docker compose down -v` y volver a arrancar |
| `python3: command not found` | Python no está en PATH | Instalar Python 3 o extraer el ID manualmente del JSON |

---

<div align="center">

**Demo de Biblioteca UNSA**

*Reproducible, verificable y alineada con las reglas del dominio.*

</div>


## Flujo de solicitudes con aprobación y entrega

El flujo actual separa solicitud, aprobación con apartado de 48 horas y entrega física que inicia el préstamo. La bandeja verifica las responsabilidades por área. Consulta [el flujo y la guía de prueba](solicitudes-prestamo.md) y el ADR-011 para los estados, permisos y endpoints actualizados.


### Principal y suplente

Cada área admite un principal y un suplente. Administración permite elegir el cargo y consultar los ocupantes actuales; la confirmación rechaza cargos ocupados, incluso con solicitudes simultáneas. Véase [ADR-012](adr/ADR-012-principal-y-suplente-por-area.md) y [la guía de responsables](solicitudes-prestamo.md#principal-y-suplente-por-área).


### Revisión de cuentas

Personas permite revisar pendientes, solicitar correcciones y confirmar la validación con identificación del revisor y fecha. El titular puede corregir sus datos desde Mi cuenta. Véase [el flujo de revisión](revision-cuentas.md) y [ADR-013](adr/ADR-013-revision-administrativa-de-cuentas.md).
