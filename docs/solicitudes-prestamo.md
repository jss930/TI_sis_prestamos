# Solicitudes de préstamo: aprobación y entrega

El alumno solicita un bien disponible desde Catálogo, elige uso en campus o a domicilio y consulta el resultado en **Mis solicitudes**. No se reservan fechas futuras: esta entrega implementa la recogida de bienes disponibles.

El responsable revisa **Solicitudes pendientes**, filtrando por área y estado. Puede aprobar y apartar un ejemplar concreto o rechazar con motivo. La aprobación no crea un préstamo: aparta la unidad durante **48 horas**. El alumno ve la fecha y hora límite de recogida.

Cuando el alumno recibe físicamente el bien, el responsable pulsa **Confirmar entrega e iniciar préstamo**. Se vuelven a comprobar cuenta habilitada, sanciones, préstamos vencidos, límite de préstamos y permiso de uso a domicilio. El plazo de préstamo empieza ese día. La devolución conserva el flujo existente de inspección física.

## Estados

`SOLICITADA → APROBADA → ATENDIDA`

- `DENEGADA`: rechazada con motivo.
- `CANCELADA`: el alumno cancela una solicitud pendiente o aprobada; se libera el apartado.
- `EXPIRADA`: vence el plazo de recogida; se libera el apartado, sin préstamo ni sanción automática.
- `ACEPTADA`: estado histórico anterior, ya asociado a un préstamo; no se modifica su historial.

El proceso de vencimiento se ejecuta cada 30 segundos mientras el backend está activo. Tras un reinicio también procesa los apartados vencidos. Una entrega fuera de plazo siempre se rechaza, aunque todavía no haya pasado el proceso de liberación.

## Áreas y permisos

Cada bien tiene un área persistida: Biblioteca, Deportes, Tecnología o Computación y periféricos. La administración puede corregirla desde el catálogo. Los bienes existentes reciben un área mediante V10; los nuevos toman por defecto Biblioteca para libros, Tecnología para equipos y Deportes para otros recursos. La administración debe revisar ese valor al registrar bienes de otra área.

En Administración, las casillas de capacitación y categorías preparan una selección pendiente. **Confirmar cambios** guarda la selección completa en una sola transacción; **Descartar cambios** restaura los valores guardados. Cambiar de persona no aplica las selecciones pendientes.

Una asignación vigente habilita la bandeja y las operaciones de préstamo y devolución. Para aprobar, rechazar o entregar, el backend comprueba la cuenta validada y la responsabilidad correspondiente al área del bien. Tecnología y Computación requieren capacitación técnica vigente. La administración puede revisar todas las áreas; registrar entregas también exige permiso operativo de préstamo.

Sharmely verá las solicitudes de las áreas que tenga asignadas. El alumno ve exclusivamente sus solicitudes; la consulta pública no devuelve solicitudes. Se conserva quién aprobó, cuándo aprobó, el ejemplar apartado, quién entregó, cuándo entregó y el préstamo resultante.

## Principal y suplente por área

Cada área admite **un principal y un suplente como máximo**. En Administración se selecciona la persona, el área y el cargo, y se pulsa **Confirmar cambios**. El resumen muestra ambos cargos por área y las plazas vacantes. El formulario queda limpio al guardar.

Un cargo ocupado no se puede duplicar. Para reemplazar a alguien, primero retira o cambia su asignación y confirma; después asigna a la nueva persona. Ambos cargos tienen las mismas facultades para atender el área y requieren las mismas condiciones de validación y capacitación. Una persona puede ser principal de un área y suplente de otra. Retirar al principal no promueve automáticamente al suplente.

V11 conserva a las personas ya asignadas y distribuye los cargos según la antigüedad de sus asignaciones. El historial registra el cargo de las nuevas asignaciones y sus cambios.

## API

| Acción | Método y ruta |
|---|---|
| Consultar solicitudes visibles para la sesión | `GET /api/v1/reservas` |
| Solicitar | `POST /api/v1/reservas` |
| Aprobar y apartar | `POST /api/v1/reservas/{id}/aceptar` |
| Confirmar entrega | `POST /api/v1/reservas/{id}/entregar` |
| Rechazar con motivo | `POST /api/v1/reservas/{id}/denegar` |
| Cancelar como solicitante | `POST /api/v1/reservas/{id}/cancelar` |
| Confirmar áreas y cargos de un responsable | `PUT /api/v1/responsables/{usuarioId}/asignaciones` |
| Cambiar área como administrador | `PATCH /api/v1/catalogo/{id}/area` |

Se mantiene `/reservas` para compatibilidad con la API existente. Las operaciones usan la identidad del token, aunque los cuerpos antiguos incluyan identificadores de usuario.

## Puesta en marcha y validación

Reinicia el backend para que Flyway aplique V10. No borres los volúmenes ni reinicies los datos. Actualiza la página; la interfaz consulta cambios cada 30 segundos.

```bash
cd backend
mvn test
mvn spring-boot:run
```

```bash
cd frontend
npm run build
npm run dev
```

Las pruebas incluyen persistencia, permisos por área, separación aprobación/entrega, cancelación, vencimiento, una entrega rechazada que revierte la liberación y dos aprobaciones simultáneas que compiten por la misma unidad. H2 adapta solo la sintaxis `ON CONFLICT (id)` de los seeds mediante configuración exclusiva de tests; las migraciones PostgreSQL ya publicadas no se reescriben.
