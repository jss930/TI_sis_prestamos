# Arquitectura del Sistema de Préstamos Universitario

**Universidad Nacional de San Agustín de Arequipa**<br>
**Escuela Profesional de Ciencia de la Computación**<br>
**Curso: Trabajo Interdisciplinario II**

---

## 1. Explicación sencilla

La arquitectura organiza el sistema para que cada parte tenga una responsabilidad clara.

```text
Frontend React
      │ HTTP/JSON
      ▼
Controladores REST
      ▼
Casos de uso
      ▼
Dominio y reglas
      ▼
Puertos de repositorio
      ▼
Adaptadores JPA
      ▼
PostgreSQL
```

La regla más importante es que el negocio no depende de la tecnología. Una reserva sabe cuándo es válida sin conocer React, HTTP, Spring o PostgreSQL.

---

## 2. Estilo arquitectónico

El sistema combina:

- **Domain-Driven Design:** divide el negocio en contextos con vocabulario propio.
- **Arquitectura hexagonal:** el dominio define puertos y la infraestructura los implementa.
- **Monolito modular:** una aplicación desplegable, con módulos internos separados.

Esta combinación es apropiada para el proyecto porque mantiene una operación sencilla y una estructura defendible. No se necesitan microservicios para demostrar buenos límites.

---

## 3. Capas

### 3.1 Dominio

Contiene las reglas que siempre deben cumplirse.

```text
<contexto>/domain/
├── model/        agregados, entidades y estados
├── vo/           objetos de valor e identificadores
├── policy/       políticas del negocio
├── event/        hechos importantes
└── repository/   puertos de persistencia
```

Ejemplos:

- `Reserva` protege sus cambios de estado.
- `PoliticaHorarioReserva` valida anticipación, duración y jornada.
- `Prestamo` evita un autopréstamo.
- `Ejemplar` controla sus estados físicos.

El dominio no importa Spring, JPA, Hibernate, Jackson ni controladores.

### 3.2 Aplicación

Coordina una acción completa.

```text
<contexto>/application/
├── command/      datos necesarios para escribir
├── port/         servicios externos requeridos
└── usecase/      pasos de la operación
```

Ejemplo de `SolicitarReservaUseCase`:

1. obtiene al usuario;
2. comprueba que esté validado;
3. obtiene el título;
4. comprueba sanciones y límites;
5. busca disponibilidad para el intervalo;
6. crea la reserva mediante el dominio;
7. guarda y publica eventos.

### 3.3 Infraestructura

Conecta el negocio con tecnología concreta:

- controladores REST;
- seguridad por token;
- entidades y repositorios JPA;
- adaptadores PostgreSQL e in-memory;
- publicación de eventos con Spring;
- configuración, CORS, reloj y datos demo.

### 3.4 Presentación web

El frontend React:

- muestra pantallas según cuenta y permisos;
- envía solicitudes HTTP;
- presenta errores amigables;
- evita errores antes de enviar;
- nunca reemplaza las validaciones del backend.

---

## 4. Contextos delimitados

### `catalog`

Describe el recurso: título, tipo y detalles bibliográficos o técnicos.

```text
CatalogoItem
├── LIBRO  → ISBN, autor, editorial, año
├── EQUIPO → marca y modelo
└── OTRO   → descripción
```

No guarda stock. La disponibilidad pertenece a los ejemplares.

### `inventory`

Administra cada unidad física.

```text
Ejemplar
├── barcode
├── ubicación
└── estado físico
```

Estados principales: `DISPONIBLE`, `PRESTADO`, `DANADO`, `PERDIDO`, `EN_REVISION` y `BAJA`.

### `identity`

Administra cuentas institucionales:

- correo y código institucional;
- estado de cuenta;
- perfil de préstamo;
- permisos operativos;
- revisión y validación administrativa.

### `reservations`

Administra solicitudes futuras por título:

- horario inicial y final;
- lugar de uso;
- aprobación o denegación;
- copia asignada;
- plazo de recogida;
- cancelación, expiración y entrega.

### `circulation`

Administra la salida física de una copia:

- préstamo directo o proveniente de una reserva;
- renovación;
- devolución;
- inspección física.

### `penalties`

Administra sanciones y reacciona a devoluciones tardías mediante eventos de dominio.

### Relación entre contextos

Los contextos comparten identificadores tipados, no objetos internos completos:

```text
Reserva ── CatalogoItemId ──► catalog
Reserva ── UsuarioId ───────► identity
Reserva ── EjemplarId ──────► inventory
Reserva ── PrestamoId ──────► circulation
```

En Java se usan `UsuarioId`, `EjemplarId` y `ReservaId` en lugar de UUID sueltos para evitar confundir identificadores de tipos diferentes.

---

## 5. Agregados e invariantes

Un agregado es un objeto que protege reglas internas.

| Agregado | Invariantes principales |
|---|---|
| `Usuario` | Solo una cuenta validada solicita; permisos solo a validados; no autovalidación |
| `CatalogoItem` | Detalle compatible con el tipo; ISBN válido |
| `Ejemplar` | Solo un disponible se presta; una baja no está prestada |
| `Reserva` | Estados válidos; horario válido; recogida dentro del plazo |
| `Prestamo` | Operador distinto del prestatario; devolución coherente |
| `Sancion` | Fechas, multa y transición de estado coherentes |

Las invariantes se validan también si un controlador omite una comprobación. Esa defensa en profundidad hace al modelo más confiable.

---

## 6. Flujo de una reserva

### Solicitud

```text
ReservaNueva.tsx
  → ReservaController.solicitar
  → SolicitarReservaUseCase
  → DisponibilidadProgramadaPort
  → Reserva.solicitarProgramada
  → ReservaRepository.guardar
```

### Aprobación

```text
Administrador
  → AceptarReservaUseCase
  → bloquea la reserva
  → busca una copia programable
  → verifica préstamos y reservas solapadas
  → bloquea la copia candidata
  → vuelve a verificar
  → Reserva.aprobarProgramada
```

### Entrega

```text
Administrador confirma entrega
  → EntregarSolicitudUseCase
  → valida el plazo de recogida
  → RegistrarPrestamoUseCase
  → ejemplar pasa a PRESTADO
  → reserva pasa a ATENDIDA
```

---

## 7. Disponibilidad y concurrencia

### Regla de solapamiento

Dos intervalos `[inicioA, finA)` y `[inicioB, finB)` se cruzan cuando:

```text
inicioA < finB AND inicioB < finA
```

El final es exclusivo. Por eso una reserva que termina a las 11:00 no bloquea otra que empieza a las 11:00.

### Disponibilidad futura

`DisponibilidadProgramadaAdapter` consulta dos fuentes:

1. préstamos activos del ejemplar;
2. reservas aprobadas del ejemplar.

Una copia que está prestada hoy puede reservarse para después de su vencimiento. Una copia dañada, perdida, en revisión o de baja no puede programarse.

### Carrera por la última copia

`@Transactional` mantiene la operación atómica. Los repositorios usan `PESSIMISTIC_WRITE` para bloquear la reserva y la copia candidata.

```text
Administrador A                     Administrador B
      │                                   │
      ├── bloquea copia LIB-001            │ espera
      ├── aprueba y confirma               │
      └── commit ─────────────────────────►│ continúa
                                          ├── reconsulta
                                          └── rechaza: no disponible
```

La comprobación se repite después del bloqueo porque la disponibilidad pudo cambiar mientras la operación esperaba.

---

## 8. Persistencia

El dominio define interfaces como `ReservaRepository`. La infraestructura implementa esas interfaces con JPA.

```text
ReservaRepository                   puerto del dominio
ReservaJpaAdapter                   traductor
ReservaJpaRepository                Spring Data
ReservaJpaEntity                    representación de tabla
reservas                            tabla PostgreSQL
```

Las entidades JPA no son objetos del dominio. El adaptador traduce entre ambos modelos.

Flyway crea y actualiza el esquema. Hibernate usa `ddl-auto=validate`, por lo que comprueba que las tablas coincidan con las entidades sin modificar el esquema automáticamente.

---

## 9. Seguridad

### Implementación actual

- Las contraseñas se guardan con PBKDF2, sal aleatoria y comparación constante.
- El login genera un token HMAC-SHA256 con duración de ocho horas.
- El frontend envía `Authorization: Bearer <token>`.
- Los flujos protegidos obtienen el actor desde el token.
- El dominio comprueba estado y permisos del usuario.
- CORS solo admite el frontend local en `5173`.

### Alcance

La seguridad permite demostrar autenticación y permisos localmente. Algunos endpoints administrativos todavía dependen de comprobaciones en sus casos de uso y no existe un filtro global de Spring Security.

Para una instalación institucional se requiere:

- Spring Security en todas las rutas;
- secreto externo y rotación de claves;
- HTTPS;
- tokens de renovación o cookies seguras;
- revocación de sesiones;
- limitación de intentos;
- auditoría persistente;
- verificación del correo institucional.

Esta limitación no afecta las reglas de reservas, préstamos o base de datos, pero impide publicar la aplicación actual directamente en Internet.

---

## 10. Eventos de dominio

Un evento expresa algo que ya ocurrió:

```text
PrestamoDevueltoConRetrasoEvent
                 │
                 ▼
PrestamoDevueltoConRetrasoListener
                 │
                 ▼
crear Sancion
```

El agregado no conoce al listener. Solo registra el evento. La infraestructura lo publica dentro de la transacción.

---

## 11. Errores

El backend devuelve `ProblemDetail`:

| Tipo | HTTP | Ejemplo |
|---|---:|---|
| Recurso inexistente | 404 | `RECURSO_NO_ENCONTRADO` |
| Regla de negocio | 409 | `EJEMPLAR_NO_DISPONIBLE` |
| Datos inválidos | 400 | `HORARIO_INVALIDO` |
| Sesión requerida | 401 | `SESION_REQUERIDA` |
| Acceso denegado | 403 | `ACCESO_DENEGADO` |

El campo `codigo` es estable. El frontend lo transforma en un mensaje comprensible.

---

## 12. Pruebas

| Nivel | Qué protege |
|---|---|
| Dominio | Invariantes y estados |
| Aplicación | Coordinación de reglas y repositorios |
| Persistencia | Traducción JPA y esquema Flyway |
| Integración | API, cuentas y concurrencia |
| Arquitectura | Dependencias permitidas |
| Frontend | Tipos y construcción de producción |

`ArquitecturaTest` impide que el dominio dependa de infraestructura. `ConcurrenciaSolicitudesTest` demuestra la carrera por la última copia. `DisponibilidadProgramadaAdapterTest` demuestra el cálculo por intervalo.

La suite actual contiene 103 pruebas backend y se ejecuta con `mvn test`.

---

## 13. Estructura del backend

```text
backend/src/main/java/com/biblioteca/
├── catalog/
├── inventory/
├── identity/
├── reservations/
├── circulation/
├── penalties/
├── shared/
└── infrastructure/
    ├── config/
    ├── events/
    ├── persistence/
    │   ├── inmemory/
    │   └── jpa/
    ├── reservations/
    ├── security/
    └── web/
```

