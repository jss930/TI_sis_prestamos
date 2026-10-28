# Guía de sustentación y ruta de estudio

## 1. Presentación de 30 segundos

> El Sistema de Préstamos Universitario administra cuentas, catálogo, copias físicas, reservas horarias, préstamos, devoluciones y sanciones. Se construyó como un monolito modular con DDD y arquitectura hexagonal. El usuario reserva un título para un intervalo futuro; el administrador aprueba y el sistema asigna una copia libre. PostgreSQL, transacciones y bloqueos evitan conflictos simultáneos.

## 2. Presentación de 5 minutos

1. **Problema:** evitar préstamos y reservas contradictorias.
2. **Usuarios:** alumno, docente, administrativo y administrador por permiso.
3. **Flujo:** cuenta provisional → validación → reserva → aprobación → entrega → devolución.
4. **Modelo:** título separado de copia física.
5. **Arquitectura:** seis contextos y cuatro capas.
6. **Base de datos:** PostgreSQL, UUID, FK, índices y Flyway.
7. **Concurrencia:** bloqueo pesimista sobre la última copia.
8. **Calidad:** pruebas backend, build frontend y GitHub Actions.
9. **Alcance:** demostración académica local; seguridad pública requiere endurecimiento.

## 3. Guion de demo

1. Abrir catálogo sin iniciar sesión.
2. Entrar con Ana y mostrar `PROVISIONAL`.
3. Mostrar que no puede reservar.
4. Entrar con administrador y validar una cuenta.
5. Entrar con usuario validado y crear una reserva.
6. Mostrar las reglas del formulario.
7. Entrar con administrador y aprobar.
8. Mostrar copia asignada y límite de recogida.
9. Confirmar entrega.
10. Mostrar el préstamo activo.
11. Abrir Adminer y enseñar las filas de `reservas` y `prestamos`.
12. Enseñar GitHub Actions o ejecutar tests.

## 4. Preguntas de Ingeniería de Software

### ¿Por qué DDD?

Porque coloca las reglas en un modelo explícito y divide el negocio en contextos. Facilita pruebas, mantenimiento y explicación.

### ¿Por qué arquitectura hexagonal?

Porque el dominio define puertos y no depende de PostgreSQL o Spring. Se puede probar con repositorios en memoria.

### ¿Por qué monolito modular?

Porque el alcance no necesita microservicios. Un despliegue único es simple y los módulos conservan límites claros.

### ¿Qué es un agregado?

Objeto que controla cambios y protege invariantes. `Reserva` evita transiciones inválidas y calcula su expiración.

### ¿Qué es una invariante?

Regla que nunca debe romperse. Ejemplo: la hora final debe ser posterior al inicio.

### ¿Qué es un caso de uso?

Clase que coordina una acción. Obtiene datos, llama al dominio, guarda y publica eventos.

### ¿Por qué no hay una entidad apartado?

Porque una reserva aprobada ya representa el compromiso temporal. Otra entidad duplicaría estado y reglas.

### ¿Por qué aprobación manual?

Porque la institución conserva control antes de asignar una copia.

### ¿Por qué no lista de espera?

Porque exige orden, prioridad y notificaciones. El alcance elegido rechaza cuando no hay disponibilidad.

### ¿Cómo aplican usabilidad?

Visibilidad de estados, prevención de errores, consistencia, retroalimentación, control del usuario y accesibilidad.

## 5. Preguntas de Base de Datos I

### ¿Por qué separar `catalogo_items` y `ejemplares`?

Un título puede tener muchas copias. El título comparte datos bibliográficos; cada copia tiene código, ubicación y estado.

### ¿Qué cardinalidad existe?

Un título tiene muchos ejemplares. Un usuario tiene muchas reservas y préstamos.

### ¿Qué integridad existe?

PK UUID, FK entre tablas, `UNIQUE` para correo/ISBN/barcode y `CHECK` para intervalos.

### ¿Dónde se guarda el stock?

No hay contador. Se deriva contando ejemplares disponibles.

### ¿Está normalizada?

La separación principal sigue 3FN. Los permisos CSV y los detalles por tipo son decisiones simples que pueden normalizarse si crece el sistema.

### ¿Qué JOIN mostrarías?

Reserva + usuario + catálogo, o préstamo + usuario + ejemplar + catálogo.

## 6. Preguntas de Base de Datos II

### ¿Qué transacción importante existe?

La aprobación y la entrega. Deben guardar varias decisiones como una unidad atómica.

### ¿Qué problema de concurrencia resuelven?

Dos administradores pueden aprobar la última copia al mismo tiempo.

### ¿Cómo lo resuelven?

Bloqueo `PESSIMISTIC_WRITE`, segunda consulta de disponibilidad y commit transaccional.

### ¿Cuál es la fórmula de solapamiento?

```text
inicioA < finB AND inicioB < finA
```

### ¿Por qué usar índices?

Para no recorrer todas las filas al buscar reservas por copia, estado y horario.

### ¿Qué es `EXPLAIN ANALYZE`?

Muestra cómo PostgreSQL ejecuta una consulta, cuánto tarda y si usa índices.

### ¿Qué es Flyway?

Sistema de migraciones que aplica versiones SQL en orden y registra cuáles ya se ejecutaron.

## 7. Preguntas de seguridad

### ¿Cómo se guardan contraseñas?

Con PBKDF2, sal aleatoria y no como texto plano.

### ¿Cómo se identifica al usuario?

Login con correo/contraseña y token firmado enviado como Bearer.

### ¿Está listo para Internet?

No. Es una demostración local. Faltan Spring Security global, HTTPS, secretos externos, revocación, auditoría y rate limiting.

### ¿Por qué decirlo claramente?

Porque una sustentación técnica debe distinguir una funcionalidad demostrable de una operación institucional segura.

## 8. Preguntas generales

### ¿Qué pasa si no recoge?

La reserva expira una hora después del inicio, sin sanción y sin lista de espera.

### ¿Puede reservar un equipo a domicilio?

No. Equipos y otros bienes son solo campus. Los libros permiten ambas opciones.

### ¿Puede elegir copia?

No. Elige título; el sistema asigna copia al aprobar.

### ¿Puede reprogramar después de aprobada?

No. Solo una solicitud pendiente y hasta una hora antes del inicio.

### ¿Qué ocurre con horarios consecutivos?

No se solapan. El fin de uno puede coincidir con el inicio del siguiente.

### ¿Una reserva cuenta para el límite?

Una reserva aprobada cuenta junto con los préstamos activos al aprobar nuevas solicitudes.

## 9. Ruta de estudio desde cero

### Día 1: producto

- Leer README.
- Dibujar título, copia, reserva y préstamo.
- Ejecutar la aplicación.

### Día 2: flujo

- Registrar o usar una cuenta.
- Crear, aprobar y entregar una reserva.
- Explicar los estados sin mirar apuntes.

### Día 3: arquitectura

- Estudiar capas y contextos.
- Seguir `ReservaController` hasta PostgreSQL.
- Aprender agregado, política, puerto y adaptador.

### Día 4: base de datos

- Dibujar las relaciones.
- Ejecutar cinco consultas SQL.
- Entender solapamiento e índices.

### Día 5: concurrencia y tests

- Entender `PESSIMISTIC_WRITE`.
- Ejecutar `mvn test`.
- Identificar qué protege ArchUnit.

### Día 6: frontend, seguridad y CI

- Seguir una petición desde React.
- Memorizar el alcance de seguridad.
- Leer `.github/workflows/ci.yml`.

### Día 7: ensayo

- Hacer demo completa.
- Responder estas preguntas en voz alta.
- Preparar un plan alterno si Docker o Internet fallan.

## 10. Lista mínima para memorizar

```text
6 contextos
4 capas
2 flujos: reserva futura y préstamo directo
2 h de anticipación
24 h máximo
07:00–20:00
1 h de tolerancia
0 lista de espera
PostgreSQL + Flyway + JPA
Transacción + bloqueo + segunda comprobación
```
