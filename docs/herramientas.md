# Herramientas del proyecto

## 1. Mapa rápido

```text
Código backend       Java 21
Framework backend    Spring Boot
Construcción         Maven
Base de datos        PostgreSQL
Persistencia         JPA + Hibernate
Migraciones          Flyway
Frontend             React + TypeScript
Servidor frontend    Vite
Servicios locales    Docker Compose
Pruebas              JUnit + AssertJ + ArchUnit + H2
Automatización        GitHub Actions
```

## 2. Backend

### Java 21

Es el lenguaje del backend. Aporta tipos, clases, records, enums, excepciones y manejo seguro de fechas.

### Spring Boot

Arranca la aplicación y conecta sus componentes. Se usa para:

- API REST;
- inyección de dependencias;
- transacciones;
- configuración;
- tareas programadas;
- endpoint de salud.

### Spring Web

Convierte métodos Java en endpoints mediante `@RestController`, `@GetMapping` y `@PostMapping`.

### Bean Validation

Valida el formato inicial de las peticiones con `@NotNull`, `@NotBlank`, `@Email` y otras anotaciones.

### Maven

Descarga dependencias, compila y ejecuta pruebas.

```bash
./mvnw compile
./mvnw test
./mvnw spring-boot:run
```

El wrapper permite usar la versión configurada sin instalar Maven globalmente.

## 3. Persistencia

### PostgreSQL 16

Base relacional principal. Ofrece transacciones ACID, claves foráneas, índices y bloqueos.

### JPA

Especificación Java para persistencia. Define entidades y operaciones de repositorio.

### Hibernate

Implementa JPA y genera SQL a partir de las operaciones Java.

### Spring Data JPA

Reduce código repetitivo en repositorios y permite consultas declarativas.

### Flyway

Versiona la estructura de la base con archivos `V1__...sql`, `V2__...sql`, etc.

### H2

Base liviana usada por tests. Ejecuta las migraciones y permite validar el esquema sin levantar Docker.

## 4. Frontend

### React 18

Construye la interfaz con componentes y estado.

### TypeScript 5

Agrega tipos a JavaScript. Detecta errores en propiedades, respuestas y funciones durante la compilación.

### React Router

Relaciona tareas con URLs y mantiene navegación, historial y botones de regreso.

### Vite 5

Sirve el frontend durante desarrollo y crea el paquete de producción.

```bash
pnpm dev
pnpm typecheck
pnpm build
```

### npm y pnpm

Instalan dependencias declaradas en `package.json`. El repositorio usa `pnpm-lock.yaml` como archivo reproducible para CI.

## 5. Desarrollo local

### Docker Compose

Levanta PostgreSQL y Adminer con un comando:

```bash
docker compose up -d
```

### Adminer

Interfaz web para ver tablas y ejecutar SQL en `http://localhost:8081`.

### Actuator

Informa si la API está funcionando:

```text
GET /actuator/health
```

### curl

Permite probar la API sin usar el frontend.

## 6. Pruebas

### JUnit 5

Ejecuta tests Java con `@Test`.

### AssertJ

Ofrece aserciones legibles:

```java
assertThat(resultado.estado()).isEqualTo(EstadoReserva.APROBADA);
```

### ArchUnit

Prueba la arquitectura. Impide que el dominio importe infraestructura o frameworks.

### Pruebas de integración

Arrancan Spring, JPA, H2 y Flyway para comprobar que todos los componentes funcionan juntos.

## 7. Automatización

### Git

Registra cambios y permite trabajar con ramas y commits.

### GitHub

Almacena el repositorio y ejecuta automatizaciones.

### GitHub Actions

El workflow `.github/workflows/ci.yml` ejecuta:

```text
Backend:  checkout → Java 21 → Maven verify
Frontend: checkout → Node 20 → pnpm install → typecheck → build
```

Esto es integración continua, porque cada cambio se integra y verifica automáticamente.

## 8. CI frente a CD

- **CI:** compilar y probar cada cambio. Está configurado.
- **CD:** desplegar automáticamente una versión aprobada. Requiere servidor, secretos, HTTPS, backups y estrategia de rollback.

El siguiente nivel de CD puede construir imágenes Docker, publicarlas y desplegarlas después de que CI termine correctamente.
