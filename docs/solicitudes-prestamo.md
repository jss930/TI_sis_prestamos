# Reservas horarias y préstamos directos

## 1. Vocabulario

- **Reserva:** solicitud para usar un título en un intervalo futuro.
- **Reserva aprobada:** compromiso temporal sobre una copia asignada.
- **Préstamo:** entrega física de una copia a una persona.
- **Préstamo directo:** entrega en ventanilla sin reserva previa.

El usuario reserva un título. Nunca selecciona una copia concreta.

## 2. Acceso

1. La persona registra una cuenta institucional.
2. La cuenta queda `PROVISIONAL` y puede consultar catálogo y cuenta.
3. La administración revisa sus datos.
4. Una cuenta `VALIDADO` puede reservar.
5. Solo un administrador con `ADMINISTRAR_SISTEMA` aprueba, deniega y entrega reservas.

Los perfiles son alumno, docente y administrativo. Administrador es un acceso concedido por permiso.

## 3. Datos solicitados

La pantalla pide:

- título;
- fecha y hora inicial;
- fecha y hora final;
- lugar de uso cuando es un libro.

El usuario sale del token de sesión. No se permite escoger otra cuenta ni una copia.

## 4. Horario

- Anticipación mínima: 2 horas.
- Horario: 07:00–20:00 en `America/Lima`.
- Fin posterior al inicio.
- Duración máxima: 24 horas.
- Libros: campus o domicilio.
- Equipos y otros bienes: campus.

El frontend previene errores y el backend vuelve a validar todo.

## 5. Disponibilidad

Una copia puede programarse cuando su estado físico lo permite y no existe un préstamo ni reserva aprobada que se cruce con el intervalo.

```text
solapamiento = inicioA < finB AND inicioB < finA
```

Una copia prestada en este momento puede reservarse para después de su vencimiento. Una copia dañada, perdida, en revisión o de baja no se programa.

No existe lista de espera. Si ninguna copia puede cubrir todo el intervalo, la solicitud se rechaza.

## 6. Estados

```text
SOLICITADA ──aprobar──> APROBADA ──entregar──> ATENDIDA
     │                      │
     ├──denegar──> DENEGADA ├──no recoge──> EXPIRADA
     └──cancelar────────────┴──cancelar──> CANCELADA
```

## 7. Aprobación

1. El administrador abre la bandeja.
2. Revisa persona, título, horario y lugar.
3. El sistema busca una copia libre.
4. Bloquea la copia candidata.
5. Vuelve a comprobar la disponibilidad.
6. Asigna la copia y marca la reserva `APROBADA`.

La copia no cambia a un estado físico `RESERVADO`. El compromiso vive en la reserva y se calcula por intervalo. Esto permite usar una misma copia en horarios consecutivos.

## 8. Recogida y expiración

- La entrega puede comenzar en la hora inicial.
- No se permite recoger antes.
- El plazo termina una hora después del inicio.
- Un proceso programado revisa expiraciones cada 30 segundos.
- La expiración no genera sanción.
- No se asigna la copia a otra persona mediante una cola.

## 9. Reprogramación

El titular puede reprogramar únicamente una reserva `SOLICITADA`.

- Debe hacerlo antes de la última hora previa al inicio original.
- El nuevo inicio debe conservar 2 horas de anticipación.
- Se validan duración, jornada y disponibilidad.
- Una reserva aprobada no se reprograma.

## 10. Cancelación

El titular puede cancelar una reserva `SOLICITADA` o `APROBADA`. No puede cancelar una reserva ajena ni una ya entregada, expirada o denegada.

## 11. Límite de uso

| Perfil | Máximo |
|---|---:|
| Alumno | 3 |
| Docente | 10 |
| Administrativo | 5 |

Al aprobar se suman préstamos activos y reservas aprobadas. Una reserva pendiente también se controla con el límite de reservas activas.

## 12. Préstamo directo

Cuando una persona llega sin reserva:

1. un operador con permiso registra el préstamo;
2. selecciona una copia concreta;
3. el sistema verifica cuenta, sanciones, vencidos, límite y lugar;
4. comprueba que el intervalo no invada una reserva aprobada;
5. crea el préstamo.

No se crea una reserva artificial para este flujo.

## 13. API

| Acción | Método y ruta |
|---|---|
| Listar reservas visibles | `GET /api/v1/reservas` |
| Crear | `POST /api/v1/reservas` |
| Reprogramar | `PATCH /api/v1/reservas/{id}/horario` |
| Aprobar | `POST /api/v1/reservas/{id}/aceptar` |
| Entregar | `POST /api/v1/reservas/{id}/entregar` |
| Denegar | `POST /api/v1/reservas/{id}/denegar` |
| Cancelar | `POST /api/v1/reservas/{id}/cancelar` |
| Préstamo directo | `POST /api/v1/prestamos` |

Ejemplo:

```json
{
  "catalogoItemId": "UUID-DEL-TITULO",
  "lugarUso": "EN_CAMPUS",
  "inicioReserva": "2026-10-28T15:00:00Z",
  "finReserva": "2026-10-28T17:00:00Z"
}
```

El backend obtiene el titular real del token. La petición no acepta el identificador de otra cuenta ni una copia elegida por el usuario.

## 14. Usabilidad

- Reglas visibles antes de enviar.
- Selección de domicilio solo para libros.
- Resumen antes de confirmar.
- Estados y plazos legibles.
- Mensajes de progreso, éxito y error.
- Cancelación y reprogramación visibles solo cuando corresponden.
- Foco y etiquetas accesibles.

## 15. Archivos clave

| Tema | Archivo |
|---|---|
| Reglas de horario | `PoliticaHorarioReserva.java` |
| Estados | `Reserva.java` |
| Solicitud | `SolicitarReservaUseCase.java` |
| Aprobación | `AceptarReservaUseCase.java` |
| Disponibilidad | `DisponibilidadProgramadaAdapter.java` |
| Entrega | `EntregarSolicitudUseCase.java` |
| Expiración | `ExpiracionSolicitudesConfig.java` |
| API | `ReservaController.java` |
| Formulario web | `ReservaNueva.tsx` |
| Bandeja admin | `Reservas.tsx` |
