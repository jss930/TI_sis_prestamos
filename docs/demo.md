# Demo reproducible con `curl`

Esta demo usa el perfil `demo`, que carga datos con IDs fijos y un préstamo vencido.
Todos los datos viven en memoria: **reiniciar la aplicación restaura el escenario inicial**.

## 0. Arrancar

### Linux / macOS

```bash
cd backend
./mvnw spring-boot:run
```

### Windows (PowerShell)

```powershell
cd backend

# Si JAVA_HOME no está configurado:
$env:JAVA_HOME = "C:\ruta\a\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

.\mvnw.cmd spring-boot:run
```

En otra terminal:

```bash
export BASE=http://localhost:8080/api/v1
curl -s http://localhost:8080/actuator/health
# {"status":"UP"}
```

> Los comandos de esta demo usan sintaxis bash (`export`, `$BASE`). En Windows puedes
> ejecutarlos desde **Git Bash** o **WSL**, o adaptarlos a PowerShell:
>
> ```powershell
> $BASE = "http://localhost:8080/api/v1"
> curl.exe -s http://localhost:8080/actuator/health
> curl.exe -s "$BASE/catalogo" | ConvertFrom-Json | ConvertTo-Json -Depth 5
> ```
>
> `curl.exe` viene incluido en Windows 10/11. En PowerShell, `curl` sin `.exe` es un alias
> de `Invoke-WebRequest` y no acepta los mismos parámetros.

IDs que se usan en toda la demo:

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

---

## 1. Catálogo e inventario

```bash
curl -s "$BASE/catalogo"
curl -s "$BASE/inventario/ejemplares"
```

Debes ver:

- *Clean Code* (`LIBRO`) con ISBN `9780132350884`.
- *Laptop Lenovo ThinkPad T14* (`EQUIPO`).
- `LIB-001` disponible, `LIB-002` prestado, `LAP-001` disponible.

La disponibilidad no sale de un contador: sale del `estadoFisico` de cada ejemplar.

---

## 2. Una cuenta PROVISIONAL no puede prestar

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

Esperado: **HTTP 409**

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

---

## 3. Devolución tardía → sanción automática

Luis tiene el préstamo `...0301`, vencido hace 3 días.

```bash
curl -s -i -X POST "$BASE/prestamos/$PRESTAMO_VENCIDO/devolucion" \
  -H 'Content-Type: application/json' \
  -d '{"estadoFisico":"DISPONIBLE"}'
```

Esperado: **HTTP 200**

```json
{
  "id": "00000000-0000-0000-0000-000000000301",
  "estado": "DEVUELTO",
  "fechaDevolucion": "2026-10-20",
  "vencido": false,
  "diasRetraso": 3
}
```

La fecha exacta depende del día en que ejecutes la demo, pero `diasRetraso` siempre es **3**
porque el seed calcula el vencimiento como “hoy menos 3 días”.

El evento `PrestamoDevueltoConRetrasoEvent` fue consumido por `penalties`:

```bash
curl -s "$BASE/sanciones?usuarioId=$LUIS"
```

Esperado: una sanción `ACTIVA` por `RETRASO`, `diasSuspension: 3`, `multa: 3.00`.

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

Esperado: **HTTP 409**

```json
{
  "title": "Regla de negocio",
  "status": 409,
  "detail": "El prestatario tiene una sancion activa",
  "codigo": "SANCION_ACTIVA"
}
```

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

Esperado: **HTTP 409**, código `AUTOPRESTAMO_NO_PERMITIDO`.

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

Esperado: **HTTP 201**, `estado: ACTIVO`, vencimiento a 7 días (política de docente/equipo).

Si repites la operación con `$ANA` o `$LUIS` (alumnos) y un equipo a domicilio, obtendrás
`DOMICILIO_NO_PERMITIDO`.

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

Esperado: **HTTP 200**, `estado: CUMPLIDA`.

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

Esperado: **HTTP 201**, `estado: ACTIVO`, vencimiento a 7 días (alumno/libro).

---

## 8. Renovar un préstamo activo

Toma el ID del préstamo recién creado:

```bash
PRESTAMO=$(curl -s "$BASE/prestamos?prestatarioId=$LUIS&estado=ACTIVO" \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)[0]["id"])')

curl -s -i -X POST "$BASE/prestamos/$PRESTAMO/renovacion"
```

Esperado: **HTTP 200**, `renovaciones: 1` y vencimiento extendido 7 días.

Una segunda renovación para un alumno debe fallar con `LIMITE_RENOVACIONES`.

---

## 9. Alta y validación de una cuenta

```bash
NUEVO=$(curl -s -X POST "$BASE/usuarios" \
  -H 'Content-Type: application/json' \
  -d '{"email":"carla@universidad.edu.pe","nombreCompleto":"Carla Nueva","perfil":"ALUMNO"}')

echo "$NUEVO"
CARLA=$(echo "$NUEVO" | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')
```

Esperado: **HTTP 201**, `estado: PROVISIONAL`, `permisos: []`.

```bash
curl -s -X POST "$BASE/usuarios/$CARLA/validar" \
  -H 'Content-Type: application/json' \
  -d "{\"operadorId\":\"$BIBLIOTECARIO\"}"
```

Esperado: `estado: VALIDADO` y `fechaValidacion` no nula.

```bash
curl -s -X POST "$BASE/usuarios/$CARLA/permisos" \
  -H 'Content-Type: application/json' \
  -d "{\"operadorId\":\"$BIBLIOTECARIO\",\"permiso\":\"REGISTRAR_PRESTAMO\"}"
```

Esperado: `permisos: ["REGISTRAR_PRESTAMO"]`. Ahora Carla puede operar ventanilla.

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

Esperado: **HTTP 201**, `estado: ACTIVA`, `fechaFin` dos días después de `fechaInicio`.

---

## 11. Errores útiles para el frontend

Todos los errores son `ProblemDetail` y traen `codigo`:

| Código | HTTP | Significado |
|---|---:|---|
| `USUARIO_NO_HABILITADO` | 409 | Cuenta no validada |
| `OPERADOR_NO_HABILITADO` | 409 | Operador sin permiso |
| `AUTOPRESTAMO_NO_PERMITIDO` | 409 | Operador = prestatario |
| `EJEMPLAR_NO_DISPONIBLE` | 409 | Ejemplar prestado, dañado, perdido o de baja |
| `SANCION_ACTIVA` | 409 | Sanción vigente |
| `PRESTAMO_VENCIDO_PENDIENTE` | 409 | Debe devolver antes de pedir otro |
| `LIMITE_PRESTAMOS` | 409 | Máximo simultáneo alcanzado |
| `DOMICILIO_NO_PERMITIDO` | 409 | Equipo a domicilio para alumno |
| `PRESTAMO_NO_ACTIVO` | 409 | Devolución/renovación sobre préstamo cerrado |
| `LIMITE_RENOVACIONES` | 409 | Sin renovaciones disponibles |
| `VALIDACION` | 400 | Campos inválidos (`campos` en el body) |
| `PETICION_INVALIDA` | 400 | JSON o parámetro mal formado |

---

## 12. Reiniciar la demo

```bash
# Ctrl+C en la terminal de Spring Boot, luego:
./mvnw spring-boot:run
```

El seed vuelve a dejar el préstamo vencido y sin sanciones.
