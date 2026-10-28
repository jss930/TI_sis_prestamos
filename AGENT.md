# Guía técnica para desarrollar el proyecto

## 1. Identidad

**Universidad Nacional de San Agustín de Arequipa**<br>
**Escuela Profesional de Ciencia de la Computación**<br>
**Trabajo Interdisciplinario II — Sistema de Préstamos Universitario — Grupo 3**

## 2. Estado técnico

- Backend Java 21 + Spring Boot 3.2.
- Frontend React 18 + TypeScript + Vite.
- PostgreSQL 16 + JPA + Flyway.
- Seis contextos: catálogo, inventario, identidad, reservas, circulación y sanciones.
- Autenticación por token para los flujos integrados.
- CI en `.github/workflows/ci.yml`.
- Proyecto académico local; no publicar sin endurecer seguridad.

## 3. Comandos

### Backend

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

Windows:

```powershell
cd backend
mvn test
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
pnpm install
pnpm typecheck
pnpm build
pnpm dev
```

### Servicios

```bash
docker compose up -d
```

| Recurso | URL |
|---|---|
| Frontend | `http://localhost:5173` |
| API | `http://localhost:8080/api/v1` |
| Health | `http://localhost:8080/actuator/health` |
| Adminer | `http://localhost:8081` |

## 4. Reglas que no se rompen

1. El dominio no importa Spring, JPA, Hibernate, Jackson ni infraestructura.
2. JPA vive en `infrastructure/persistence/jpa`.
3. Flyway es la fuente de verdad del esquema.
4. No agregar contadores de stock al catálogo.
5. La disponibilidad se deriva de copias, préstamos y reservas.
6. Usar IDs tipados en dominio, no UUID sueltos.
7. Las reglas viven en agregados o políticas.
8. Los casos de uso coordinan; los controladores traducen HTTP.
9. El actor de acciones protegidas sale del token.
10. Mantener backend tests, frontend typecheck y build verdes.

## 5. Modelo actual

- El usuario reserva un título, no una copia.
- La aprobación es manual y exclusiva del administrador.
- La copia se asigna al aprobar.
- No existe lista de espera.
- No existe una entidad separada de apartado.
- Los permisos se conceden directamente a cada cuenta.
- Una reserva expira una hora después del inicio si no se recoge.
- La expiración no genera sanción.

## 6. Horario de reservas

- anticipación mínima: 2 horas;
- duración máxima: 24 horas;
- jornada: 07:00–20:00 en `America/Lima`;
- reprogramación: pendiente y hasta 1 hora antes;
- libros: campus o domicilio;
- equipos y otros: campus.

## 7. Capas

```text
domain          reglas y puertos
application     comandos, puertos de aplicación y casos de uso
infrastructure  web, JPA, seguridad, eventos y configuración
frontend        presentación y cliente HTTP
```

## 8. Contextos

| Contexto | Responsabilidad |
|---|---|
| `catalog` | Títulos y metadatos |
| `inventory` | Copias físicas |
| `identity` | Cuentas, estados y permisos |
| `reservations` | Horarios, aprobación y recogida |
| `circulation` | Préstamos, renovaciones y devoluciones |
| `penalties` | Sanciones |

## 9. Persistencia

- Agregados y entidades JPA son modelos distintos.
- Los adaptadores traducen entre ambos.
- Escrituras usan `@Transactional`.
- La aprobación bloquea reserva y copia con `PESSIMISTIC_WRITE`.
- El solapamiento usa `inicioA < finB && inicioB < finA`.
- Una copia `PRESTADO` puede programarse si su préstamo termina antes del intervalo.

## 10. Cómo agregar una funcionalidad

1. Identificar el contexto dueño.
2. Escribir la regla en lenguaje del negocio.
3. Agregar o modificar agregado/política.
4. Crear comando y caso de uso.
5. Definir puertos necesarios.
6. Implementar adaptadores.
7. Agregar migración Flyway si cambia el esquema.
8. Exponer API y frontend.
9. Agregar pruebas.
10. Actualizar README y documento temático.

## 11. Pruebas obligatorias

- Dominio para invariantes.
- Caso de uso para reglas compuestas.
- Persistencia para consultas y mapeos.
- Integración para concurrencia o seguridad cuando aplique.
- `ArquitecturaTest` siempre activo.

## 12. Documentación

La referencia de aprendizaje es:

- `README.md`;
- `docs/guia-del-sistema.md`;
- `docs/arquitectura.md`;
- `docs/solicitudes-prestamo.md`;
- `docs/base-de-datos.md`;
- `docs/guia-sustentacion.md`.

La documentación describe el sistema actual con lenguaje sencillo y precisión técnica. Evitar mezclar planes, versiones o modelos que no forman parte del funcionamiento vigente.
