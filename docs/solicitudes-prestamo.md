# Reservas horarias y préstamos directos

## Modelo simple

Una **reserva** es una solicitud para usar un título en una fecha y horario futuros. El usuario no elige una copia: al aprobar, el sistema asigna automáticamente un ejemplar libre que no tenga otro horario superpuesto.

Un **préstamo directo** ocurre cuando la persona se presenta en ventanilla sin reserva. El administrador selecciona una copia y registra el préstamo. La operación se rechaza si esa copia está comprometida por una reserva aprobada dentro del periodo del préstamo.

No existen áreas responsables, responsables principales/suplentes ni capacitación técnica. La administración valida cuentas y concede permisos directamente a cada usuario.

## Acceso

1. Cualquier persona con correo y código institucional puede registrar una cuenta.
2. Una cuenta `PROVISIONAL` inicia sesión y solo consulta el catálogo y sus datos.
3. El administrador revisa y valida la cuenta.
4. Una cuenta `VALIDADO` puede crear, consultar, cancelar y reprogramar sus reservas.
5. Solo una cuenta validada con `ADMINISTRAR_SISTEMA` puede aprobar, denegar y entregar reservas.

Los perfiles de negocio son alumno, docente y administrativo. Administrador es un acceso derivado del permiso `ADMINISTRAR_SISTEMA`, no un perfil de préstamo distinto.

## Reglas del horario

- Inicio con al menos **2 horas de anticipación**.
- Atención entre **07:00 y 20:00**, zona `America/Lima`.
- La hora final debe ser posterior a la inicial.
- Duración máxima de **24 horas**.
- La reserva se realiza por título.
- Libros: uso en campus o domicilio.
- Equipos y otros bienes: solo dentro del campus.
- No existe lista de espera.
- Si no hay una copia libre para todo el intervalo, la operación se rechaza.
- Horarios consecutivos no se consideran superpuestos; por ejemplo, 10:00–11:00 y 11:00–12:00.
- Una reserva aprobada cuenta dentro del límite de préstamos/apartados.

## Estados

```text
SOLICITADA ──aprobar──> APROBADA ──entregar──> ATENDIDA
     │                      │
     ├──denegar──> DENEGADA ├──no recoge +1 h──> EXPIRADA
     └──cancelar────────────┴──cancelar──> CANCELADA
```

`ACEPTADA` se conserva únicamente para leer registros históricos de la versión anterior.

La aprobación no cambia el estado físico del ejemplar a `RESERVADO`: el compromiso es temporal y vive en la reserva. Así una misma copia puede tener reservas no superpuestas. Cuando se entrega, el ejemplar pasa a `PRESTADO` y se crea el préstamo.

## Reprogramación y vencimiento

- El titular puede reprogramar una reserva `SOLICITADA` hasta una hora antes del inicio original.
- El nuevo horario vuelve a validar anticipación, jornada y duración.
- Una reserva aprobada se puede recoger desde su hora inicial.
- Si no se recoge dentro de la hora siguiente al inicio, expira automáticamente.
- La expiración no genera sanción y no crea lista de espera.

## Concurrencia

La aprobación bloquea la reserva y la copia candidata con `PESSIMISTIC_WRITE`. Después del bloqueo se vuelve a consultar si existe solapamiento. Si dos administradores intentan asignar la última copia al mismo horario, solo la primera aprobación termina; la segunda recibe `EJEMPLAR_NO_DISPONIBLE`.

## API

| Acción | Método y ruta |
|---|---|
| Ver reservas visibles | `GET /api/v1/reservas` |
| Crear reserva | `POST /api/v1/reservas` |
| Reprogramar | `PATCH /api/v1/reservas/{id}/horario` |
| Aprobar y asignar copia | `POST /api/v1/reservas/{id}/aceptar` |
| Confirmar entrega | `POST /api/v1/reservas/{id}/entregar` |
| Denegar | `POST /api/v1/reservas/{id}/denegar` |
| Cancelar | `POST /api/v1/reservas/{id}/cancelar` |

Ejemplo de creación:

```json
{
  "prestatarioId": "UUID-IGNORADO-POR-COMPATIBILIDAD",
  "catalogoItemId": "UUID-DEL-TITULO",
  "lugarUso": "EN_CAMPUS",
  "inicioReserva": "2026-10-28T15:00:00Z",
  "finReserva": "2026-10-28T17:00:00Z"
}
```

La identidad real se obtiene del token. `prestatarioId` se conserva temporalmente en el DTO para compatibilidad, pero no decide el titular.

## Persistencia

`V13__reservas_con_horario.sql` agrega `inicio_reserva`, `fin_reserva` y un índice de consulta. Las migraciones anteriores no se modifican porque Flyway las considera historial inmutable. Las tablas históricas de responsables pueden seguir presentes en bases ya migradas, pero no tienen endpoint ni interfaz y ya no participan en las reglas.

## Usabilidad aplicada

- **Visibilidad:** reglas y estado se muestran antes de confirmar.
- **Prevención de errores:** mínimos de fecha, controles deshabilitados y validación cliente/servidor.
- **Correspondencia con el mundo real:** reservar, aprobar, recoger y devolver son acciones separadas.
- **Consistencia:** se usa “reserva” en navegación y mensajes.
- **Retroalimentación:** cada acción muestra progreso, éxito o error comprensible.
- **Control del usuario:** cancelar y reprogramar tienen condiciones visibles.
- **Accesibilidad:** etiquetas, foco visible, estados ARIA y botones con texto explícito.

## Verificación

```bash
cd backend
./mvnw test

cd ../frontend
pnpm typecheck
pnpm build
```

La línea base actual es **100 pruebas backend verdes** y compilación TypeScript/Vite correcta.
