# Base de datos: modelo, SQL, DB1 y DB2

## 1. Explicación sencilla

PostgreSQL guarda la información permanente. Cada concepto importante tiene una tabla y cada fila se identifica con un UUID.

```text
catalogo_items  describe títulos
ejemplares      describe copias físicas
usuarios        describe cuentas
credenciales    guarda hashes de contraseña
reservas        guarda solicitudes futuras
prestamos       guarda entregas físicas
sanciones       guarda restricciones
inspecciones_devolucion guarda revisiones físicas
```

## 2. Relaciones

```text
catalogo_items 1 ───── N ejemplares
catalogo_items 1 ───── N reservas
usuarios       1 ───── 1 credenciales
usuarios       1 ───── N reservas
usuarios       1 ───── N prestamos
usuarios       1 ───── N sanciones
ejemplares     1 ───── N prestamos
ejemplares     1 ───── N reservas aprobadas
prestamos      1 ─── 0..1 inspeccion_devolucion
prestamos      1 ───── N sanciones
```

## 3. Tablas principales

### `usuarios`

Guarda identidad, perfil, estado y permisos. `email` y `codigo_institucional` son únicos.

### `catalogo_items`

Guarda título, tipo y detalles. Las columnas de libro y equipo son opcionales según el tipo.

### `ejemplares`

Guarda la copia física. `barcode` es único y `catalogo_item_id` enlaza con su título.

### `reservas`

Guarda:

- usuario solicitante;
- título solicitado;
- intervalo inicial y final;
- lugar de uso;
- estado;
- copia asignada al aprobar;
- administrador y fecha de decisión;
- plazo y fecha de entrega;
- préstamo creado.

### `prestamos`

Guarda operador, prestatario, ejemplar, fechas, horas, lugar de uso, estado y renovaciones.

## 4. Integridad

### Claves primarias

Cada tabla principal usa UUID. Esto evita mezclar identificadores y permite generarlos desde la aplicación.

### Claves foráneas

Evitan referencias inexistentes. Una reserva no puede apuntar a un usuario o título que no existe.

### Unicidad

- correo institucional;
- código institucional;
- ISBN;
- código de barras.

### Restricciones

La base comprueba que el fin de una reserva sea posterior al inicio. El dominio agrega reglas más ricas: anticipación, jornada y duración.

## 5. Migraciones Flyway

Flyway aplica los archivos `V1` a `V15` en orden y registra cada versión en `flyway_schema_history`.

Regla práctica:

> Una migración aplicada no se edita. Un nuevo cambio se expresa en una nueva versión.

Hibernate usa `ddl-auto=validate`: verifica el esquema, pero Flyway sigue siendo la fuente de verdad.

## 6. Consultas SQL útiles

### Catálogo y cantidad de copias disponibles ahora

```sql
SELECT c.id, c.titulo, c.tipo,
       COUNT(e.id) FILTER (WHERE e.estado_fisico = 'DISPONIBLE') AS disponibles
FROM catalogo_items c
LEFT JOIN ejemplares e ON e.catalogo_item_id = c.id
WHERE c.activo = TRUE
GROUP BY c.id, c.titulo, c.tipo
ORDER BY c.titulo;
```

### Reservas pendientes con persona y título

```sql
SELECT r.id, u.nombre_completo, c.titulo,
       r.inicio_reserva, r.fin_reserva, r.estado
FROM reservas r
JOIN usuarios u ON u.id = r.prestatario_id
JOIN catalogo_items c ON c.id = r.catalogo_item_id
WHERE r.estado = 'SOLICITADA'
ORDER BY r.inicio_reserva;
```

### Solapamiento de una copia

```sql
SELECT r.id, r.inicio_reserva, r.fin_reserva
FROM reservas r
WHERE r.estado = 'APROBADA'
  AND r.ejemplar_id = :ejemplar_id
  AND r.inicio_reserva < :fin_solicitado
  AND r.fin_reserva > :inicio_solicitado;
```

La condición significa: cada intervalo empieza antes de que termine el otro.

### Préstamos activos

```sql
SELECT p.id, u.nombre_completo, c.titulo, e.barcode,
       p.fecha_hora_entrega, p.fecha_hora_vencimiento
FROM prestamos p
JOIN usuarios u ON u.id = p.prestatario_id
JOIN ejemplares e ON e.id = p.ejemplar_id
JOIN catalogo_items c ON c.id = e.catalogo_item_id
WHERE p.estado = 'ACTIVO'
ORDER BY p.fecha_hora_vencimiento;
```

### Uso del límite por persona

```sql
SELECT u.nombre_completo,
       COUNT(DISTINCT p.id) FILTER (WHERE p.estado = 'ACTIVO') AS prestamos,
       COUNT(DISTINCT r.id) FILTER (WHERE r.estado = 'APROBADA') AS apartados
FROM usuarios u
LEFT JOIN prestamos p ON p.prestatario_id = u.id
LEFT JOIN reservas r ON r.prestatario_id = u.id
GROUP BY u.id, u.nombre_completo
ORDER BY u.nombre_completo;
```

### Sanciones activas

```sql
SELECT u.nombre_completo, s.motivo, s.fecha_inicio, s.fecha_fin, s.multa
FROM sanciones s
JOIN usuarios u ON u.id = s.usuario_id
WHERE s.estado = 'ACTIVA';
```

## 7. Índices

Un índice es parecido al índice de un libro: permite encontrar datos sin leer todas las filas.

El sistema incluye índices para:

- ejemplares por título;
- préstamos por usuario, copia y estado;
- reservas por usuario, estado, título y horario;
- sanciones por usuario y estado;
- cuentas por estado de revisión.

Los índices de horario priorizan `ejemplar_id`, estado e intervalo para las consultas de conflicto.

Para analizar una consulta:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM reservas
WHERE estado = 'APROBADA'
  AND ejemplar_id = :id
  AND inicio_reserva < :fin
  AND fin_reserva > :inicio;
```

## 8. Transacciones y bloqueo

Los casos de uso de escritura usan `@Transactional`.

Propiedades ACID:

- **Atomicidad:** todo se guarda o nada se guarda.
- **Consistencia:** las reglas y restricciones se mantienen.
- **Aislamiento:** operaciones simultáneas no se mezclan incorrectamente.
- **Durabilidad:** después del commit, PostgreSQL conserva los datos.

Bloqueo de una fila:

```sql
BEGIN;
SELECT * FROM ejemplares WHERE id = :id FOR UPDATE;
-- comprobar y guardar
COMMIT;
```

JPA expresa este comportamiento con `PESSIMISTIC_WRITE`.

## 9. DB1 frente a DB2

| Base de Datos I | Aplicación en el proyecto |
|---|---|
| Entidades y relaciones | Usuarios, títulos, copias, reservas y préstamos |
| PK y FK | UUID y referencias entre tablas |
| Cardinalidad | Un título tiene muchas copias |
| Normalización | Separación de catálogo, inventario y circulación |
| SQL básico | SELECT, JOIN, WHERE, GROUP BY |
| Restricciones | NOT NULL, UNIQUE, CHECK |

| Base de Datos II | Aplicación en el proyecto |
|---|---|
| Transacciones | `@Transactional` |
| Concurrencia | Dos aprobaciones simultáneas |
| Bloqueos | `PESSIMISTIC_WRITE` / `FOR UPDATE` |
| Índices | Consultas por estado, copia y horario |
| Plan de ejecución | `EXPLAIN ANALYZE` |
| Migraciones | Flyway |
| Integridad avanzada | Segunda comprobación después del bloqueo |

## 10. Preguntas rápidas

### ¿Por qué separar título y copia?

Porque un título puede tener varias copias con estados distintos.

### ¿Dónde está el stock?

Se calcula desde los ejemplares; no existe un contador duplicado.

### ¿Por qué UUID?

Permite generar identificadores fuera de la base y evita confundir IDs secuenciales. Su costo es un índice más grande.

### ¿Cómo se evita una doble reserva?

Consulta de solapamiento, transacción, bloqueo de la copia y segunda consulta.

### ¿Los permisos están normalizados?

Se guardan como texto separado por comas para mantener simple el agregado. Una instalación mayor podría usar una tabla `usuario_permisos`.

### ¿Cómo se prueba la base?

Los tests usan H2 en modo PostgreSQL y ejecutan las mismas migraciones. La aplicación real usa PostgreSQL 16.
