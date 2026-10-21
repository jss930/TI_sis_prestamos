# Biblioteca UNSA — Sistema de Préstamos

Reescritura del proyecto `TI_sis_prestamos` con arquitectura DDD por contextos delimitados.

> **Estado actual: fase 2 completada.**
> Backend con PostgreSQL + Spring Data JPA + Flyway, **58 tests verdes** y API REST
> documentada. El frontend React + TypeScript + Vite ya consume `/api/v1`.
> La autenticación JWT, los roles reales y las reservas quedan para las siguientes fases.

El estado anterior del repositorio se conserva en el tag **`legacy-v0`**. El código legacy
está archivado en `docs/legacy/`; no se intentó reparar porque no compilaba y tenía dos
fuentes de verdad para el inventario.

---

## 1. Qué funciona hoy

- Dominio puro en Java 21: agregados, value objects como `record`, eventos e invariantes.
- Cinco contextos: `catalog`, `inventory`, `identity`, `circulation` y `penalties`.
- Puertos de repositorio en el dominio y adaptadores JPA en infraestructura, sin contaminar
  las entidades de dominio. Los adaptadores in-memory siguen disponibles para pruebas.
- Esquema versionado con Flyway (`V1__schema.sql`) para los cinco contextos.
- Casos de uso transaccionales con `@Transactional`; el dominio no importa Spring ni JPA.
- API REST `/api/v1` con validación y errores `ProblemDetail` (`application/problem+json`).
- Frontend React + TypeScript + Vite integrado con la API, con manejo de errores por `codigo`.
- Datos demo con IDs fijos y un préstamo vencido para demostrar el flujo completo.
- Reglas de negocio aplicadas en el dominio, no en controladores.
- Tests unitarios, de aplicación, de flujo completo, de persistencia JPA y de arquitectura
  (ArchUnit). Los tests usan H2 en modo PostgreSQL para no depender de Docker.

---

## 2. Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.2.0 (Web, Validation, Actuator, Data JPA) |
| Build | Maven Wrapper (`./mvnw` en Linux/macOS, `mvnw.cmd` en Windows) |
| Tests | JUnit 5, AssertJ, ArchUnit, Spring Boot Test, H2 |
| Persistencia | PostgreSQL 16 + Spring Data JPA + Flyway |
| Frontend | React 18 + TypeScript + Vite 5 |

No se usa Lombok ni MapStruct. JPA vive únicamente en `infrastructure/persistence/jpa`;
el dominio no importa Spring, Jakarta Persistence, Hibernate ni Jackson, y ArchUnit lo
verifica en cada build.

---

## 3. Cómo ejecutar

### Requisitos

- **JDK 21** instalado. Verifica que `java -version` muestre `21.x`.
- **Maven no es necesario**: el repositorio incluye Maven Wrapper.
- `JAVA_HOME` debe apuntar al JDK 21. En Linux/macOS `mvnw` puede detectarlo desde
  `java`/`javac`, pero es recomendable definirlo. En Windows, `mvnw.cmd` lo requiere.
- **Docker + Docker Compose** para levantar PostgreSQL (backend por defecto).
- **Node.js 18 o superior** y `pnpm` (recomendado) o `npm` para el frontend.
- Git y conexión a internet la primera vez (Maven/npm descargan dependencias).

### Configurar Java 21

**Linux**

```bash
export JAVA_HOME=/ruta/a/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

**macOS**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

**Windows PowerShell**

```powershell
$env:JAVA_HOME = "C:\ruta\a\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

**Windows CMD**

```bat
set "JAVA_HOME=C:\ruta\a\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
java -version
```

> `C:\ruta\a\jdk-21` y `/ruta/a/jdk-21` son ejemplos. Usa la ruta real de tu instalación;
> no copies rutas de la máquina de otro compañero.

### Verificar el Maven Wrapper

Los archivos `mvnw` (Unix) y `mvnw.cmd` (Windows) están en `backend/`, junto con
`.mvn/wrapper/`.

**Linux / macOS**

```bash
cd backend
ls -l mvnw mvnw.cmd .mvn/wrapper/
./mvnw -v
```

**Windows PowerShell**

```powershell
cd backend
dir mvnw, mvnw.cmd, .mvn\wrapper
.\mvnw.cmd -v
```

### Levantar PostgreSQL

Desde la raíz del repositorio:

```bash
docker compose up -d
docker compose ps
```

Servicios:

- PostgreSQL: `localhost:5432` (base/usuario/clave: `biblioteca`).
- Adminer: <http://localhost:8081>.
- Datos persistentes en el volumen `biblioteca_pgdata`.

Para detenerlos: `docker compose down`. Para borrar también los datos:
`docker compose down -v`.

### Compilar y probar

**Linux / macOS**

```bash
cd backend
./mvnw clean compile
./mvnw test
```

**Windows (PowerShell o CMD)**

```powershell
cd backend
.\mvnw.cmd clean compile
.\mvnw.cmd test
```

Los tests usan H2 en modo PostgreSQL y ejecutan la misma migración Flyway, por lo que no
necesitan Docker. Resultado esperado:

```text
BUILD SUCCESS
Tests run: 58, Failures: 0, Errors: 0, Skipped: 0
```

### Levantar la API

Con PostgreSQL ya iniciado:

**Linux / macOS**

```bash
cd backend
./mvnw spring-boot:run
```

**Windows**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

- API: <http://localhost:8080/api/v1>
- Health: <http://localhost:8080/actuator/health>
- Perfil por defecto: `demo` (crea los datos de ejemplo sobre PostgreSQL).
- Flyway aplica `V1__schema.sql` automáticamente al arrancar.
- Para arrancar sin datos demo: `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`
  (en Windows, `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod`).
- La conexión se puede sobreescribir con `DB_URL`, `DB_USER` y `DB_PASSWORD`.

La demo completa con `curl` está en [`docs/demo.md`](docs/demo.md).

### Levantar el frontend

```bash
cd frontend
pnpm install
pnpm dev
```

Abre <http://localhost:5173>. El frontend lee `VITE_API_BASE_URL` desde `.env`
(por defecto `http://localhost:8080/api/v1`). El catálogo puede verse sin sesión; las
operaciones de ventanilla usan el modo demo porque la autenticación real aún no existe.

---

## 4. Datos demo (IDs fijos)

| Recurso | ID | Notas |
|---|---|---|
| Bibliotecario (Elena) | `00000000-0000-0000-0000-000000000001` | `VALIDADO`, todos los permisos |
| Ana (alumna) | `00000000-0000-0000-0000-000000000002` | `PROVISIONAL`, no puede prestar ni operar |
| Luis (alumno) | `00000000-0000-0000-0000-000000000003` | `VALIDADO`, tiene un préstamo vencido |
| Rosa (docente) | `00000000-0000-0000-0000-000000000004` | `VALIDADO`, puede llevar equipos a domicilio |
| Libro *Clean Code* | `00000000-0000-0000-0000-000000000101` | ISBN `9780132350884` |
| Laptop Lenovo T14 | `00000000-0000-0000-0000-000000000102` | Equipo |
| Ejemplar `LIB-001` | `00000000-0000-0000-0000-000000000201` | Disponible |
| Ejemplar `LIB-002` | `00000000-0000-0000-0000-000000000202` | Prestado a Luis (vencido) |
| Ejemplar `LAP-001` | `00000000-0000-0000-0000-000000000203` | Disponible |
| Préstamo vencido | `00000000-0000-0000-0000-000000000301` | Vence 3 días antes de “hoy” |

El seed se ejecuta con el perfil `demo` solo si la tabla `usuarios` está vacía. Los datos
persisten en PostgreSQL entre reinicios; si quieres volver al escenario inicial, ejecuta
`docker compose down -v && docker compose up -d` antes de arrancar de nuevo.

---

## 5. API principal

Base: `http://localhost:8080/api/v1`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/catalogo?q=&tipo=` | Lista/busca ítems del catálogo |
| `GET` | `/catalogo/{id}` | Obtiene un ítem |
| `POST` | `/catalogo/libros` | Registra un libro |
| `POST` | `/catalogo/equipos` | Registra un equipo |
| `POST` | `/catalogo/otros` | Registra otro tipo de ítem |
| `POST` | `/catalogo/{id}/desactivar` | Saca un ítem de circulación |
| `GET` | `/inventario/ejemplares?catalogoItemId=&barcode=` | Lista ejemplares |
| `GET` | `/inventario/ejemplares/{id}` | Obtiene un ejemplar |
| `POST` | `/inventario/ejemplares` | Registra una unidad física |
| `POST` | `/inventario/ejemplares/{id}/baja` | Da de baja un ejemplar |
| `GET` | `/usuarios?q=&estado=&perfil=` | Lista/busca cuentas |
| `GET` | `/usuarios/{id}` | Obtiene una cuenta |
| `POST` | `/usuarios` | Registra una cuenta `PROVISIONAL` |
| `POST` | `/usuarios/{id}/validar` | Valida la cuenta (operador) |
| `POST` | `/usuarios/{id}/suspender` | Suspende la cuenta (operador + motivo) |
| `POST` | `/usuarios/{id}/reactivar` | Reactiva la cuenta (operador) |
| `POST` | `/usuarios/{id}/permisos` | Otorga permiso operativo |
| `DELETE` | `/usuarios/{id}/permisos/{permiso}?operadorId=` | Revoca permiso operativo |
| `GET` | `/prestamos?prestatarioId=&estado=` | Lista préstamos |
| `GET` | `/prestamos/{id}` | Obtiene un préstamo |
| `POST` | `/prestamos` | Registra un préstamo |
| `POST` | `/prestamos/{id}/devolucion` | Registra devolución (estado físico) |
| `POST` | `/prestamos/{id}/renovacion` | Renueva si no está vencido |
| `GET` | `/sanciones?usuarioId=&estado=` | Lista sanciones |
| `POST` | `/sanciones` | Registra sanción manual |
| `POST` | `/sanciones/{id}/cumplir` | Marca la sanción como cumplida |

Los errores usan `application/problem+json` (`ProblemDetail`) e incluyen un campo
`codigo` estable para el frontend, por ejemplo `SANCION_ACTIVA`, `USUARIO_NO_HABILITADO`
o `AUTOPRESTAMO_NO_PERMITIDO`.

---

## 6. Reglas de negocio implementadas

### Identidad

- `PROVISIONAL` puede consultar el catálogo, pero **no** solicitar préstamos ni operar ventanilla.
- Un permiso operativo no salta la regla anterior: primero hay que validar la cuenta.
- Transiciones permitidas: `PROVISIONAL → VALIDADO → SUSPENDIDO → VALIDADO`.
- La suspensión exige motivo.
- Solo una cuenta `VALIDADO` puede recibir permisos operativos.

### Circulación

- El prestatario debe existir y estar `VALIDADO`.
- El operador debe estar `VALIDADO` y tener el permiso `REGISTRAR_PRESTAMO`.
- `operadorId != prestatarioId` (no hay autopréstamo).
- El ítem debe estar activo y el ejemplar disponible.
- No puede haber dos préstamos activos sobre el mismo ejemplar.
- No se presta si el usuario tiene una sanción activa.
- No se presta si el usuario tiene un préstamo vencido pendiente.
- Se respeta el máximo de préstamos simultáneos por perfil.
- Los equipos solo salen a domicilio para docentes y administrativos.
- Un préstamo vencido puede devolverse, pero no renovarse.
- “Vencido” se deriva de las fechas; no se guarda como estado.

### Sanciones

- Una devolución tardía publica `PrestamoDevueltoConRetrasoEvent`.
- `penalties` reacciona y crea una `Sancion` (1 día de suspensión y S/ 1.00 por día,
  con tope de 30 días).
- Una sanción activa bloquea nuevos préstamos.
- La sanción puede cumplirse o anularse; no cambia el estado de la cuenta.

### Política de plazos

| Perfil | Libro | Equipo | Otro | Máx. simultáneos | Renovaciones |
|---|---:|---:|---:|---:|---:|
| Alumno | 7 días | 1 día | 3 días | 3 | 1 |
| Docente | 30 días | 7 días | 7 días | 10 | 2 |
| Administrativo | 15 días | 3 días | 5 días | 5 | 2 |

---

## 7. Estructura del repositorio

```text
TI_sis_prestamos/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/biblioteca/
│       │   ├── shared/domain/          # AggregateRoot, ValueObject, DomainEvent, excepciones
│       │   ├── shared/application/     # DomainEventPublisher (puerto)
│       │   ├── catalog/                # Catálogo e ítems (sin stock numérico)
│       │   ├── inventory/              # Ejemplares físicos y códigos de barras
│       │   ├── identity/               # Cuentas, estados y permisos
│       │   ├── circulation/            # Préstamos, devoluciones, renovaciones
│       │   ├── penalties/              # Sanciones y multas
│       │   └── infrastructure/         # JPA, in-memory, Spring, REST
│       │       ├── persistence/jpa/    # Entidades, repositorios y adaptadores JPA
│       │       ├── persistence/inmemory/ # Adaptadores para tests
│       │       └── web/                # Controladores y ProblemDetail
│       ├── main/resources/
│       │   ├── application.yml         # PostgreSQL/JPA/Flyway por defecto
│       │   └── db/migration/V1__schema.sql
│       └── test/java/com/biblioteca/   # 58 tests + ArchUnit
├── docs/
│   ├── arquitectura.md
│   ├── demo.md
│   ├── adr/
│   └── legacy/                         # SQL del proyecto anterior (solo referencia)
├── frontend/                           # React + TypeScript + Vite integrado con /api/v1
├── docker-compose.yml                  # PostgreSQL 16 + Adminer
└── AGENT.md                            # Guía para futuros agentes/desarrolladores
```

Cada contexto sigue la misma forma:

```text
<contexto>/
├── domain/
│   ├── model/          # Agregados, entidades, value objects, enums
│   ├── event/          # Eventos de dominio
│   ├── policy/         # Reglas configurable (sin Spring)
│   ├── repository/     # Puertos de salida (interfaces)
│   └── vo/             # IDs tipados y value objects
├── application/
│   ├── command/        # Intenciones de escritura
│   └── usecase/        # Orquestación; no contiene reglas de negocio
└── infrastructure/     # Solo si el contexto tiene adaptadores propios
```

---

## 8. Decisiones de diseño clave

1. **Reescritura limpia, no parcheo.** El código legacy no compilaba y mezclaba capas.
   Se archivó en el tag `legacy-v0`.
2. **El catálogo no tiene stock.** `CatalogoItem` describe la obra o equipo; `Ejemplar`
   es la unidad física prestable. La disponibilidad se deriva del estado del ejemplar y
   del préstamo activo.
3. **IDs UUID tipados.** `UsuarioId`, `CatalogoItemId`, `EjemplarId`, `PrestamoId` y
   `SancionId` evitan mezclar identificadores y mantienen el dominio independiente de la BD.
4. **Eventos de dominio detrás de un puerto.** Hoy se publican de forma síncrona con
   Spring; mañana se pueden cambiar por outbox o un broker sin tocar el dominio.
5. **Sanciones desacopladas.** `circulation` no conoce `penalties`; solo publica el evento.
6. **JPA solo en infraestructura.** Las entidades técnicas se mapean a los agregados en
   adaptadores explícitos; el dominio sigue sin conocer la base de datos. Flyway es la
   fuente de verdad del esquema y H2 permite correr los tests sin Docker.

Detalle completo en [`docs/arquitectura.md`](docs/arquitectura.md) y en los ADR de
[`docs/adr/`](docs/adr/).

---

## 9. Tests

```bash
cd backend
./mvnw test
```

Cobertura actual:

- Value objects: ISBN, email, nombres, códigos de barras.
- Agregados: `Usuario`, `Ejemplar`, `Prestamo`, `Sancion`.
- Casos de uso: registrar préstamo, devolver, renovar, sancionar.
- Flujo completo sin Spring: préstamo → devolución tardía → evento → sanción → bloqueo.
- Contexto Spring con JPA + H2 (`@SpringBootTest`).
- Persistencia JPA: los cinco adaptadores guardan, reconstruyen y consultan sus agregados
  sobre el esquema Flyway (`JpaPersistenciaTest`).
- Reglas de arquitectura: dominio sin framework, puertos como interfaces, aplicación
  sin infraestructura.

---

## 10. Roadmap

| Fase | Alcance | Estado |
|---|---|---|
| 1. Dominio + API en memoria | Contextos, reglas, REST, tests, demo | ✅ |
| 2. Persistencia | JPA, PostgreSQL, Flyway, transacciones | ✅ |
| 3. Frontend integrado | React + TS + Vite, cliente HTTP, ProblemDetail, operaciones | ✅ |
| 4. Seguridad | JWT, roles, sesiones reales, catálogo público con identidad | ⏳ |
| 5. Operación | Testcontainers/outbox, CI/CD, observabilidad, backups | ⏳ |

**Alcance confirmado para la siguiente fase:** login solo para personal bibliotecario y
administradores; catálogo público para alumnos; `identity` se mantiene unificado y
preparado para habilitar el acceso de alumnos sin cambios de esquema. Las reservas quedan
fuera del alcance principal (ver [ADR-008](docs/adr/ADR-008-reservas-fuera-de-alcance.md)).

### Pendientes conocidos

- No hay autenticación real: el login del frontend es demo y los `operadorId` del body se
  confían; JWT y roles quedan para la fase 4.
- Los eventos se entregan de forma síncrona en el mismo proceso; falta outbox/async y
  pruebas con Testcontainers/PostgreSQL real.
- Los endpoints de catálogo e inventario aún no exigen el permiso `GESTIONAR_CATALOGO`.
- No hay reservas, paginación, bloqueo optimista ni auditoría persistente.

---

## 11. Documentación

- [Arquitectura y contextos](docs/arquitectura.md)
- [Demo con curl](docs/demo.md)
- [ADRs](docs/adr/)
- [Guía para agentes](AGENT.md)
- [Esquema legacy](docs/legacy/) — solo referencia histórica

---

## 12. Autoría y contexto académico

Proyecto de Ingeniería de Software / Biblioteca UNSA. La reescritura prioriza que las
reglas del negocio sean explícitas, testeables y defendibles en una sustentación, por
encima de acumular endpoints o pantallas sin coherencia.
