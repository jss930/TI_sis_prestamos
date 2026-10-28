# Base de datos

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

## 6. Índices

Un índice es parecido al índice de un libro: permite encontrar datos sin leer todas las filas.

El sistema incluye índices para:

- ejemplares por título;
- préstamos por usuario, copia y estado;
- reservas por usuario, estado, título y horario;
- sanciones por usuario y estado;
- cuentas por estado de revisión.

## 7. Transacciones y bloqueo

Los casos de uso de escritura usan `@Transactional`.

Propiedades ACID:

- **Atomicidad:** todo se guarda o nada se guarda.
- **Consistencia:** las reglas y restricciones se mantienen.
- **Aislamiento:** operaciones simultáneas no se mezclan incorrectamente.
- **Durabilidad:** después del commit, PostgreSQL conserva los datos.


