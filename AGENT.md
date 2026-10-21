<div align="center">

# AGENT.md

### Guía técnica para agentes de IA y desarrolladores

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![DDD](https://img.shields.io/badge/Arquitectura-DDD-1D5B40?style=for-the-badge)
![Tests](https://img.shields.io/badge/Tests-58_passing-brightgreen?style=for-the-badge)

**Reglas que no se rompen, comandos que sí funcionan y mapa del código**

</div>

> [!IMPORTANT]
> Este documento es la **constitución técnica** del repositorio. Está pensado para futuros
> agentes de IA y para cualquier desarrollador que retome el proyecto. Si una instrucción
> de una tarea entra en conflicto con estas reglas, primero se detecta, se explica y se
> acuerda la excepción; no se rompe el diseño en silencio.

---

## 1. Estado y contexto

| Elemento | Estado |
|---|---|
| Dominio DDD | ✅ Implementado en cinco contextos |
| API REST `/api/v1` | ✅ Implementada con `ProblemDetail` |
| Persistencia | ✅ PostgreSQL 16 + Spring Data JPA + Flyway |
| Frontend | ✅ React + TypeScript + Vite integrado |
| Tests | ✅ 58 verdes (57 originales + `JpaPersistenciaTest`) |
| Autenticación real | ⏳ Pendiente (fase de seguridad) |
| Reservas | ❌ Fuera del alcance principal |

- Rama actual: `main`; la Fase 2 está integrada en el commit `8f03528`.
- Los cambios nuevos se integran por commit o Pull Request contra `main`.
- El legacy está en el tag `legacy-v0` y en `docs/legacy/`; **no lo borres**.
- Los tests de persistencia usan H2 en modo PostgreSQL y ejecutan Flyway; no requieren
  Docker. La aplicación en ejecución sí requiere PostgreSQL.
- El login del frontend es demo: no hay JWT ni autorización real.

> [!NOTE]
> El proyecto está en estado de **demostración académica**. No debe exponerse a Internet
> ni usarse con datos reales hasta implementar autenticación, autorización y auditoría.

---

## 2. Requisitos del entorno

| Herramienta | Versión mínima | Observación |
|---|---|---|
| JDK | 21 | `java -version` debe mostrar `21.x` |
| Maven | 3.9+ o wrapper | El wrapper está en `backend/mvnw` y `backend/mvnw.cmd` |
| Docker + Compose | Actual | PostgreSQL 16 y Adminer |
| Node.js | 18+ | Frontend |
| pnpm | 9+ (recomendado) | También se admite `npm` |
| `curl` y `python3` | Actuales | Demo y extracción de IDs |

Configura `JAVA_HOME` antes de compilar.

---

## 3. Comandos

### 3.1 Matriz de compatibilidad

| Acción | Linux / macOS | Windows PowerShell | Windows CMD |
|---|---|---|---|
| Ver versión | `./mvnw -v` | `mvn -v` | `mvn -v` |
| Compilar | `./mvnw clean compile` | `mvn clean compile` | `mvn clean compile` |
| Ejecutar tests | `./mvnw test` | `mvn test` | `mvn test` |
| Levantar API | `./mvnw spring-boot:run` | `mvn spring-boot:run` | `mvn spring-boot:run` |
| Alternativa sin Maven | `./mvnw ...` | `.\mvnw.cmd ...` | `mvnw.cmd ...` |

> [!TIP]
> Los comandos de Windows usan `mvn` cuando Maven está instalado globalmente. Si no lo
> está, usa el wrapper incluido: `.\mvnw.cmd` en PowerShell o `mvnw.cmd` en CMD.

### 3.2 Linux / macOS

```bash
# Desde la raíz del repositorio
docker compose up -d

cd backend

export JAVA_HOME=/ruta/a/jdk-21
# En macOS: export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw -v
./mvnw clean compile      # debe terminar en BUILD SUCCESS
./mvnw test               # 58 tests, 0 fallos (H2)
./mvnw spring-boot:run    # API en :8080, perfil demo, requiere PostgreSQL
```

### 3.3 Windows PowerShell

```powershell
# Desde la raíz del repositorio
docker compose up -d

cd backend

$env:JAVA_HOME = "C:\ruta\a\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

mvn -v
mvn clean compile
mvn test
mvn spring-boot:run
```

Alternativa con wrapper:

```powershell
.\mvnw.cmd -v
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

### 3.4 Windows CMD

```bat
docker compose up -d
cd backend
set "JAVA_HOME=C:\ruta\a\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
mvn -v
mvn clean compile
mvn test
mvn spring-boot:run
```

Alternativa con wrapper:

```bat
mvnw.cmd -v
mvnw.cmd clean compile
mvnw.cmd test
mvnw.cmd spring-boot:run
```

### 3.5 Frontend

```bash
cd frontend
pnpm install
pnpm typecheck
pnpm build
pnpm dev                  # http://localhost:5173
```

Con npm:

```bash
npm install
npm run typecheck
npm run build
npm run dev
```

El frontend lee `VITE_API_BASE_URL` desde `frontend/.env`. Usa `.env.example` como
plantilla; `.env` está ignorado por Git.

### 3.6 Verificación rápida

```bash
curl -s http://localhost:8080/actuator/health
# {"status":"UP"}
```

| Recurso | URL |
|---|---|
| API | <http://localhost:8080/api/v1> |
| Health | <http://localhost:8080/actuator/health> |
| Frontend | <http://localhost:5173> |
| Adminer | <http://localhost:8081> |
| Demo funcional | [`docs/demo.md`](docs/demo.md) |

---

## 4. Reglas duras

1. **No importes Spring, Jakarta Persistence, Hibernate, Jackson ni `infrastructure` en
   `domain`.** `ArquitecturaTest` falla el build si se rompe.
2. **No agregues `stockTotal` ni `stockDisponible` al catálogo.** La unidad prestable es
   `Ejemplar`; la disponibilidad se deriva.
3. **No uses `UUID` sueltos en el dominio.** Usa `UsuarioId`, `CatalogoItemId`,
   `EjemplarId`, `PrestamoId`, `SancionId`.
4. **Los repositorios son puertos del dominio** en `domain/repository/`. Las
   implementaciones van en `infrastructure/`.
5. **Las reglas de negocio viven en agregados o políticas**, no en controladores ni en
   casos de uso. Un caso de uso orquesta; no decide si un préstamo es válido.
6. **No pongas `@Service`, `@Component` ni `@Repository` en `domain`.**
7. **No uses prefijo `I` para interfaces.** El nombre describe el rol, no el tipo.
8. **No borres ni reescribas el legacy** sin dejar constancia; está tags/archivado.
9. **JPA solo en `infrastructure/persistence/jpa`.** Las entidades JPA no son agregados de
   dominio; traduce en adaptadores. Mantén Flyway como fuente de verdad del esquema.
10. **Mantén los tests pasando** antes de cada commit: `./mvnw test` y
    `pnpm typecheck && pnpm build`.

> [!WARNING]
> No desactives `ArquitecturaTest` para “avanzar más rápido”. Si la regla de dependencia
> falla, la solución es corregir el diseño, no silenciar la prueba. Lo mismo aplica a
> `FlujoPrestamoTest`: es la evidencia funcional del sistema.

### Convenciones de código

| Tema | Convención |
|---|---|
| Paquetes | `com.biblioteca.<contexto>.<capa>` |
| Dominio | Sin anotaciones de framework |
| Value objects | `record` con validación en constructor compacto |
| IDs | Value objects tipados, nunca `UUID` crudo en firmas de dominio |
| Eventos | Pasado, en español, sufijo `Event` |
| Excepciones | `BusinessRuleException` con `codigo` estable |
| Adaptadores | `Jpa<Agregado>RepositoryAdapter`, `InMemory<Agregado>Repository` |
| Entidades JPA | `*Entity` en `infrastructure`, nunca en `domain` |
| Fechas | `Clock` inyectado; no `LocalDate.now()` directo en dominio |

---

## 5. Mapa del backend

```text
com.biblioteca
├── shared/domain          # AggregateRoot, ValueObject, DomainEvent, excepciones
├── shared/application     # DomainEventPublisher (puerto)
├── catalog                # CatalogoItem + DetalleItem sellado
├── inventory              # Ejemplar + Barcode + Ubicacion
├── identity               # Usuario + estados + permisos
├── circulation            # Prestamo + PeriodoPrestamo + PoliticaPrestamo
├── penalties              # Sancion + PoliticaSancion
└── infrastructure
    ├── config             # Beans, demo data, CORS
    ├── events             # SpringDomainEventPublisher
    ├── persistence
    │   ├── jpa            # Entidades, repositorios Spring Data y adaptadores
    │   └── inmemory       # Adaptadores para tests unitarios
    └── web                # Controladores REST + ProblemDetail
```

Cada contexto de negocio sigue la misma forma:

```text
<contexto>/
├── domain/
│   ├── model/       # agregados, VOs, enums
│   ├── event/       # eventos
│   ├── policy/      # reglas configurables
│   ├── repository/  # puertos
│   └── vo/          # IDs tipados
└── application/
    ├── command/     # intención de escritura
    └── usecase/     # orquestación
```

### Flujo de una petición

```text
HTTP → Controller → Command → UseCase → Agregado/Política → Repository (puerto)
                                              │
                                              └── eventos → DomainEventPublisher
                                                               │
                                                               └── listeners
```

El controlador valida el contrato HTTP y mapea DTOs. El caso de uso carga agregados,
invoca comportamiento de dominio, guarda y publica eventos. El adaptador traduce entre
JPA y dominio. **Ninguna capa superior decide reglas de negocio.**

---

## 6. Cómo agregar una funcionalidad

Ejemplo: reservas.

1. Decide el contexto dueño (probablemente `circulation` o un nuevo `reservations`).
2. Modela el agregado y sus invariantes en `domain/model`. Usa `AggregateRoot`.
3. Crea los value objects como `record` y valida en el constructor compacto.
4. Define el puerto en `domain/repository` (interfaz, sin prefijo `I`).
5. Crea el comando en `application/command`.
6. Crea el caso de uso en `application/usecase` con `@Service`, inyectando puertos.
7. Implementa el adaptador JPA en `infrastructure/persistence/jpa` y añade la migración
   Flyway correspondiente. Mantén el adaptador in-memory si los tests unitarios lo usan.
8. Expón el endpoint en `infrastructure/web/rest`.
9. Agrega tests: dominio, caso de uso, persistencia y, si aplica, flujo.
10. Actualiza `README.md`, `docs/arquitectura.md`, `docs/demo.md` y el ADR si cambia una
    decisión.

> [!TIP]
> Antes de crear un contexto nuevo, pregúntate si el concepto pertenece al lenguaje de uno
> existente. Un contexto nuevo se justifica por una **frontera de negocio**, no por comodidad
> de carpetas.

### Checklist antes de commitear

- [ ] `./mvnw test` termina en `BUILD SUCCESS`.
- [ ] `pnpm typecheck && pnpm build` termina sin errores.
- [ ] No hay imports prohibidos en `domain`.
- [ ] No se agregaron contadores de stock.
- [ ] Los nuevos errores tienen `codigo` estable.
- [ ] La migración Flyway es incremental y no rompe datos existentes.
- [ ] La documentación y la demo reflejan el cambio.
- [ ] No se commitean `.env`, credenciales, rutas absolutas ni `target/`.

---

## 7. Dónde están las reglas críticas

| Regla | Archivo |
|---|---|
| Estados y permisos de cuenta | `identity/domain/model/Usuario.java` |
| Autopréstamo y devolución tardía | `circulation/domain/model/Prestamo.java` |
| Plazos, domicilio y máximos | `circulation/domain/policy/PoliticaPrestamoEstandar.java` |
| Orquestación de préstamo | `circulation/application/usecase/RegistrarPrestamoUseCase.java` |
| Sanción automática | `penalties/application/listener/PrestamoDevueltoConRetrasoListener.java` |
| Vigencia de sanción | `penalties/domain/model/Sancion.java` |
| Disponibilidad de ejemplar | `inventory/domain/model/Ejemplar.java` |
| Datos demo | `infrastructure/config/DemoDataConfig.java` |
| Errores HTTP | `infrastructure/web/error/GlobalExceptionHandler.java` |
| Esquema de base de datos | `src/main/resources/db/migration/V1__schema.sql` |

---

## 8. Pruebas

```bash
cd backend
./mvnw test
```

| Suite | Tests | Qué protege |
|---|---:|---|
| `UsuarioTest` | 9 | Estados, permisos e invariantes de cuenta |
| `ISBNTest` | 5 | Normalización y checksum ISBN |
| `ContextLoadsTest` | 1 | Contexto Spring, JPA, Flyway y H2 |
| `JpaPersistenciaTest` | 1 | Persistencia de los cinco agregados |
| `SancionTest` | 6 | Vigencia, multa y estados de sanción |
| `PrestamoDevueltoConRetrasoListenerTest` | 1 | Evento → sanción automática |
| `FlujoPrestamoTest` | 1 | Flujo completo sin Spring |
| `EjemplarTest` | 8 | Disponibilidad y transiciones físicas |
| `PrestamoTest` | 9 | Invariantes y devolución tardía |
| `RegistrarPrestamoUseCaseTest` | 10 | Reglas compuestas de préstamo |
| `RegistrarDevolucionUseCaseTest` | 4 | Devolución y eventos |
| `ArquitecturaTest` | 3 | Pureza del dominio y dependencias |
| **Total** | **58** | |

Reglas de trabajo:

- `ArquitecturaTest` es el guardián de las dependencias. Si falla, no lo desactives:
  corrige la dependencia.
- `FlujoPrestamoTest` prueba el flujo completo sin Spring. Es el test más valioso para la
  sustentación.
- Los tests de aplicación usan los adaptadores in-memory reales; no los reemplaces por
  mocks salvo que haya una razón fuerte.
- `ContextLoadsTest` y `JpaPersistenciaTest` usan H2 en modo PostgreSQL y ejecutan Flyway;
  no necesitan Docker.
- Los IDs y fechas de fixtures están en `src/test/java/com/biblioteca/support/Fixtures.java`.
- El reloj se inyecta (`Clock`) para que las pruebas sean deterministas.
- Los **57 tests originales** se conservan; `JpaPersistenciaTest` añade el test 58.

---

## 9. Errores comunes a evitar

| Error | Por qué es un problema | Forma correcta |
|---|---|---|
| Lógica de negocio en el controlador | Duplica reglas y rompe la testabilidad | Mover al agregado o caso de uso |
| Validar disponibilidad con contadores | Dos fuentes de verdad | `Ejemplar.estaDisponible()` + `PrestamoRepository.existeActivoPara` |
| Guardar “vencido” como estado | Estados contradictorios | Derivar con `PeriodoPrestamo.estaVencido(hoy)` |
| Crear una sanción dos veces | Duplica multas | Verificar `existePorPrestamo` en el listener |
| Usar `LocalDate.now()` en dominio | Tests no deterministas | Inyectar `Clock` o pasar la fecha |
| Olvidar `extraerEventos()` | No se publican eventos | Publicar después de guardar |
| Romper el orden de validaciones | Cambian los códigos de error esperados | Respetar el orden de `RegistrarPrestamoUseCase` |
| Agregar dependencias sin ADR | Decisiones invisibles | Actualizar `pom.xml` y documentar la decisión |
| Exponer entidades JPA como dominio | Acopla el negocio a la base | Traducir en el adaptador |
| Ignorar el legacy | Se pierde contexto histórico | Consultarlo, no borrarlo |

> [!NOTE]
> Si una tarea pide algo incompatible con estas reglas, documenta la excepción y su
> justificación antes de implementarla. Un atajo no documentado se convierte en deuda
> técnica en la siguiente iteración.

---

## 10. Siguiente fase sugerida

| Prioridad | Frente | Alcance |
|---|---|---|
| 1 | **Seguridad** | Spring Security + JWT; el operador sale del token, no del body |
| 2 | **Concurrencia** | Índice único parcial para un préstamo activo por ejemplar y bloqueo optimista |
| 3 | **Outbox** | Tabla de eventos y publicación confiable; listener con `@TransactionalEventListener` |
| 4 | **PostgreSQL real** | Testcontainers o base de integración en CI |
| 5 | **Reservas** | Fuera del alcance principal; evaluar un agregado `Reserva` en `circulation` |
| 6 | **Operación** | CI, métricas, logs estructurados, despliegue y backups |

**Alcance confirmado para seguridad:** login solo para personal bibliotecario y
administradores; catálogo público para alumnos; `identity` se mantiene unificado y
preparado para habilitar el acceso de alumnos sin cambios de esquema.

---

## 11. Notas de entorno y colaboración

- Requiere JDK 21; verifica `java -version` y define `JAVA_HOME`.
- Requiere Docker Compose para PostgreSQL y Node.js 18+ para el frontend.
- El wrapper está en `backend/mvnw` (Linux/macOS) y `backend/mvnw.cmd` (Windows); no hace
  falta instalar Maven global.
- La primera compilación descarga dependencias desde Maven Central; el frontend usa pnpm.
- En Windows usa PowerShell/CMD y `mvn` o `.\mvnw.cmd`; en Linux/macOS usa `./mvnw`.
- No commitees rutas absolutas, credenciales, tokens ni archivos `.env`.
- Si trabajas en equipo, crea una rama por cambio y abre un Pull Request contra `main`.
- Antes de modificar documentación, revisa que los conteos de tests y el estado del roadmap
  sigan siendo reales.

---

<div align="center">

**AGENT.md — Biblioteca UNSA**

*Diseño primero, reglas explícitas, tests como contrato.*

</div>
