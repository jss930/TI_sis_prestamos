# Sistema de Préstamos Universitario

<div align="center">

**Universidad Nacional de San Agustín de Arequipa**<br>
**Escuela Profesional de Ciencia de la Computación**<br>
**Curso: Trabajo Interdisciplinario II**

### GRUPO 3

| Integrantes |
|---|
| Alfaro Buiza, Jesùs Alberto |
| Quispe Chavez, Sharmely |
| Juan de Dios Delgado, Hellen Grace |
| Tito Vilca, Lizeth Angélica |

</div>

---

## 1. ¿Qué es este proyecto?

Es una aplicación web para administrar préstamos de libros, equipos y otros bienes de una universidad.

El sistema permite:

- registrar cuentas institucionales;
- validar cuentas antes de permitir reservas;
- consultar el catálogo;
- reservar un título para una fecha y hora;
- aprobar o denegar reservas;
- entregar una copia física e iniciar el préstamo;
- registrar préstamos directos en ventanilla;
- recibir devoluciones e inspeccionar el bien;
- administrar sanciones, catálogo, ejemplares y permisos.

El proyecto es un **monolito modular**: se ejecuta como una sola aplicación, pero el código está dividido por responsabilidades de negocio. Esto facilita entenderlo, probarlo y mantenerlo.

> **Idea principal:** el usuario reserva un **título**, no una copia. Cuando el administrador aprueba, el sistema asigna una copia libre para ese horario.

---

## 2. Modelo mental rápido

Para entender todo el sistema, responde estas seis preguntas:

| Pregunta | Concepto | Módulo |
|---|---|---|
| ¿Qué recurso es? | Título del catálogo | `catalog` |
| ¿Cuál unidad física es? | Ejemplar o copia | `inventory` |
| ¿Quién usa el sistema? | Cuenta, perfil y permisos | `identity` |
| ¿Cuándo se quiere usar? | Reserva futura | `reservations` |
| ¿Ya fue entregado? | Préstamo y devolución | `circulation` |
| ¿Existe una restricción? | Sanción | `penalties` |

Ejemplo:

```text
"Clean Code"                         → título del catálogo
La copia con código LIB-001           → ejemplar
Luis Quispe                           → usuario
Martes de 10:00 a 12:00              → reserva
El administrador entrega LIB-001     → préstamo
Luis devuelve fuera de plazo         → posible sanción
```

### Diferencias importantes

- **Título:** descripción general del recurso. Ejemplo: *Clean Code*.
- **Ejemplar:** copia física concreta. Ejemplo: `LIB-001`.
- **Reserva:** solicitud para usar un título en un horario futuro.
- **Préstamo:** operación que comienza cuando se entrega físicamente el ejemplar.
- **Reserva aprobada:** compromiso temporal sobre una copia. No existe otra entidad llamada “apartado”.

---

## 3. Personas, cuentas y accesos

### Estados de una cuenta

| Estado | Qué puede hacer |
|---|---|
| `PROVISIONAL` | Iniciar sesión, ver catálogo y revisar su cuenta |
| `VALIDADO` | Reservar y recibir préstamos, según sus permisos |
| `SUSPENDIDO` | No puede reservar ni recibir préstamos |

Una cuenta nueva queda `PROVISIONAL`. El administrador comprueba sus datos institucionales y la valida. La validación no concede automáticamente permisos administrativos.

### Los cuatro accesos visibles

| Acceso | Cómo se representa |
|---|---|
| Alumno | Perfil `ALUMNO` |
| Docente | Perfil `DOCENTE` |
| Administrativo | Perfil `ADMINISTRATIVO` |
| Administrador | Cuenta validada con permiso `ADMINISTRAR_SISTEMA` |

El administrador no es un perfil de préstamo distinto. Es un acceso derivado de un permiso. Así, el sistema mantiene tres políticas de préstamo y concede las funciones administrativas directamente a la cuenta correspondiente.

### Permisos por usuario

- `ADMINISTRAR_SISTEMA`
- `REGISTRAR_PRESTAMO`
- `REGISTRAR_DEVOLUCION`
- `VALIDAR_USUARIO_PROVISIONAL`
- `GESTIONAR_CATALOGO`
- `GESTIONAR_SANCIONES`
- `GESTIONAR_USUARIOS`

La administración concede o revoca permisos directamente a cada usuario.

---

## 4. Cómo funciona una reserva

### Reglas

| Regla | Valor |
|---|---|
| Anticipación mínima | 2 horas |
| Duración máxima | 24 horas |
| Horario permitido | 07:00 a 20:00, hora de Arequipa/Lima |
| Recurso seleccionado | Título, nunca una copia concreta |
| Aprobación | Manual, solo administrador |
| Lista de espera | No existe |
| Recogida | Desde la hora inicial hasta 1 hora después |
| Si no recoge | La reserva expira sin sanción |
| Reprogramación | Solo pendiente y hasta 1 hora antes del inicio |
| Equipos y otros bienes | Solo dentro del campus |
| Libros | Campus o domicilio |

### Flujo

```text
Usuario validado
      │
      ▼
Selecciona un título, fecha, hora y lugar de uso
      │
      ▼
SOLICITADA
      │
      ├── administrador deniega ──────────────► DENEGADA
      │
      ├── usuario cancela ────────────────────► CANCELADA
      │
      ▼
Administrador aprueba y el sistema asigna una copia
      │
      ▼
APROBADA
      │
      ├── no recoge hasta una hora después ──► EXPIRADA
      │
      ├── usuario cancela ────────────────────► CANCELADA
      │
      ▼
Administrador confirma la entrega
      │
      ▼
ATENDIDA + préstamo ACTIVO
```

### Disponibilidad por horario

Una copia está libre para un intervalo cuando:

1. puede programarse físicamente;
2. no tiene un préstamo que se cruce con el intervalo;
3. no tiene otra reserva aprobada que se cruce con el intervalo.

Los intervalos consecutivos no chocan:

```text
10:00–11:00 y 11:00–12:00  → permitido
10:00–11:30 y 11:00–12:00  → conflicto
```

Si dos administradores intentan aprobar la última copia al mismo tiempo, la base de datos bloquea la copia durante la decisión. La primera aprobación termina y la segunda recibe un error de disponibilidad.

---

## 5. Préstamo directo

El préstamo directo se usa cuando la persona llega a ventanilla sin una reserva.

1. El administrador selecciona a la persona.
2. Selecciona una copia física.
3. Indica entrega, vencimiento y lugar de uso.
4. El sistema comprueba cuenta validada, sanciones, préstamos vencidos, límites y reservas aprobadas.
5. Si todo es válido, crea el préstamo y marca la copia como `PRESTADO`.

No se crea una reserva para un préstamo inmediato. La operación se rechaza si la copia está comprometida por una reserva aprobada dentro del mismo horario.

---

## 6. Arquitectura en palabras sencillas

El backend usa Domain-Driven Design y arquitectura hexagonal.

```text
HTTP / Base de datos / Framework
            │
            ▼
      Infraestructura
            │
            ▼
        Aplicación
            │
            ▼
          Dominio
```

- **Dominio:** contiene las reglas de negocio.
- **Aplicación:** coordina cada operación.
- **Infraestructura:** conecta HTTP, PostgreSQL, JPA, tokens y eventos.
- **Frontend:** presenta las tareas y consume la API.

Ejemplo al reservar:

```text
ReservaNueva.tsx
  → POST /api/v1/reservas
  → ReservaController
  → SolicitarReservaUseCase
  → Reserva + PoliticaHorarioReserva
  → ReservaJpaAdapter
  → PostgreSQL
```

Más detalle: [`docs/arquitectura.md`](docs/arquitectura.md).

---

## 7. Tecnologías

| Herramienta | Uso |
|---|---|
| Java 21 | Lenguaje del backend |
| Spring Boot 3.2 | API, configuración y transacciones |
| Maven | Compilación y pruebas del backend |
| PostgreSQL 16 | Base de datos principal |
| Spring Data JPA / Hibernate | Persistencia y consultas |
| Flyway | Versionado del esquema |
| React 18 | Interfaz web |
| TypeScript 5 | Tipado del frontend |
| Vite 5 | Desarrollo y construcción del frontend |
| Docker Compose | PostgreSQL y Adminer locales |
| JUnit 5 / AssertJ | Pruebas automatizadas |
| ArchUnit | Reglas de arquitectura |
| H2 | Base de pruebas |
| GitHub Actions | Integración continua |

Explicación completa: [`docs/herramientas.md`](docs/herramientas.md).

---

## 8. Requisitos

- JDK 21.
- Maven 3.9 o Maven Wrapper incluido.
- Docker con Docker Compose.
- Node.js 20 recomendado.
- pnpm 9 recomendado; también funciona npm.

Verifica:

```bash
java -version
docker --version
node --version
```

---

## 9. Ejecutar el proyecto

### Windows PowerShell

Desde la raíz del repositorio:

```powershell
docker compose up -d
```

Backend, en otra terminal:

```powershell
cd backend
mvn spring-boot:run
```

Si Maven no está instalado globalmente:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Frontend, en otra terminal:

```powershell
cd frontend
npm install
npm run dev
```

> Maven debe ejecutarse dentro de `backend/`. Ejecutarlo en la raíz produce el error “No plugin found for prefix spring-boot”.

### Linux, WSL o macOS

```bash
docker compose up -d

cd backend
./mvnw spring-boot:run
```

En otra terminal:

```bash
cd frontend
pnpm install
pnpm dev
```

### Direcciones

| Servicio | Dirección |
|---|---|
| Frontend | <http://localhost:5173> |
| API | <http://localhost:8080/api/v1> |
| Salud | <http://localhost:8080/actuator/health> |
| Adminer | <http://localhost:8081> |
| PostgreSQL | `localhost:5432` |

Base, usuario y contraseña local: `biblioteca`.

### Cuentas de demostración

Todas usan la contraseña `password`.

| Cuenta | Situación |
|---|---|
| `bibliotecario@biblioteca.pe` | Administrador con permisos |
| `ana@universidad.edu.pe` | Alumna provisional |
| `luis@universidad.edu.pe` | Alumno validado |
| `docente@universidad.edu.pe` | Docente validada |

---

## 10. Base de datos

Las tablas principales son:

```text
usuarios ───────────┬── reservas ─── catalogo_items
                    │       │
                    │       └────── ejemplares
                    │
                    ├── prestamos ── ejemplares ── catalogo_items
                    │
                    └── sanciones

usuarios ── credenciales
```

- Las claves principales son UUID.
- Las claves foráneas conservan la integridad entre tablas.
- Flyway aplica 15 migraciones.
- JPA usa `ddl-auto=validate`: valida el esquema, no lo inventa.
- Las fechas horarias usan `TIMESTAMP WITH TIME ZONE`.
- El servidor trabaja con la zona `America/Lima` y persiste instantes en UTC.

Guía de base de datos, SQL y DB1/DB2: [`docs/base-de-datos.md`](docs/base-de-datos.md).

---

## 11. API principal

Todas las rutas comienzan con `/api/v1`.

| Módulo | Rutas principales |
|---|---|
| Autenticación | `POST /auth/login` |
| Catálogo | `GET /catalogo`, `POST /catalogo/libros`, `/equipos`, `/otros` |
| Inventario | `GET/POST /inventario/ejemplares` |
| Usuarios | `GET/POST /usuarios`, acciones de validación y permisos |
| Reservas | `GET/POST /reservas`, aprobar, denegar, cancelar, reprogramar, entregar |
| Préstamos | `GET/POST /prestamos`, devolución y renovación |
| Sanciones | `GET/POST /sanciones` |

Los errores siguen `ProblemDetail`:

```json
{
  "title": "Regla de negocio",
  "status": 409,
  "detail": "No hay ejemplares libres durante el horario seleccionado",
  "codigo": "EJEMPLAR_NO_DISPONIBLE"
}
```

La API usa un token firmado para identificar al usuario en los flujos protegidos. Es una seguridad adecuada para la demostración académica local, no para publicar el sistema en Internet. La guía técnica explica su alcance en [`docs/arquitectura.md`](docs/arquitectura.md#9-seguridad).

---

## 12. Pruebas y calidad

Backend:

```bash
cd backend
./mvnw test
```

Windows:

```powershell
cd backend
mvn test
```

Frontend:

```bash
cd frontend
pnpm typecheck
pnpm build
```

Las pruebas cubren:

- reglas de usuarios, ejemplares, préstamos, sanciones y reservas;
- casos de uso;
- persistencia JPA y migraciones Flyway;
- concurrencia sobre la última copia;
- disponibilidad futura por intervalo;
- reglas de dependencia con ArchUnit.

La suite actual ejecuta **103 pruebas backend: 0 fallos, 0 errores y 0 omitidas**.

GitHub Actions ejecuta automáticamente el backend con Java 21 y el frontend con Node 20 en cada `push` y `pull request`.

---

## 13. Usabilidad

La interfaz aplica principios básicos:

- **Visibilidad:** reglas y estados se muestran antes de confirmar.
- **Prevención de errores:** límites de fecha, opciones deshabilitadas y validación doble.
- **Consistencia:** se usa el término “reserva” en todo el flujo.
- **Retroalimentación:** muestra carga, éxito y errores comprensibles.
- **Control del usuario:** permite volver, cancelar y reprogramar cuando corresponde.
- **Accesibilidad:** etiquetas, foco visible, contraste y reducción de movimiento.

---

## 14. Documentos para estudiar

| Documento | Para qué sirve |
|---|---|
| [`docs/guia-del-sistema.md`](docs/guia-del-sistema.md) | Entender todo el sistema desde nivel cero |
| [`docs/arquitectura.md`](docs/arquitectura.md) | Capas, contextos, reglas y concurrencia |
| [`docs/solicitudes-prestamo.md`](docs/solicitudes-prestamo.md) | Reserva y préstamo directo |
| [`docs/base-de-datos.md`](docs/base-de-datos.md) | Modelo, SQL, DB1 y DB2 |
| [`docs/herramientas.md`](docs/herramientas.md) | Qué hace cada tecnología |
| [`docs/interfaz-web.md`](docs/interfaz-web.md) | Pantallas, navegación y usabilidad |
| [`docs/revision-cuentas.md`](docs/revision-cuentas.md) | Registro y validación de cuentas |
| [`docs/demo.md`](docs/demo.md) | Guion de demostración |
| [`docs/guia-sustentacion.md`](docs/guia-sustentacion.md) | Preguntas, respuestas y ruta de estudio |

### Orden recomendado

```text
README
  → guía del sistema
  → reservas
  → arquitectura
  → base de datos
  → herramientas
  → guía de sustentación
```

---

## 15. Estado académico

El sistema está preparado para ejecutarse y demostrarse localmente. Incluye frontend, API, base de datos, migraciones, datos demo, pruebas automatizadas y CI.

Para un despliegue público se deben reforzar autenticación, autorización global, gestión de secretos, HTTPS, auditoría, copias de seguridad y observabilidad. Esta separación permite presentar honestamente qué funciona en la demostración y qué requiere una operación institucional real.
