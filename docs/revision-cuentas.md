# Revisión administrativa de cuentas

## Registro y estados

El registro pide nombre, correo institucional, código universitario o de personal, perfil y contraseña. El código acepta de 4 a 30 letras, números o guiones, se normaliza a mayúsculas y no puede repetirse entre cuentas.

La cuenta nueva queda `PROVISIONAL`, que en la interfaz se muestra como **Pendiente de validación**. Puede iniciar sesión, consultar el catálogo y revisar **Mi cuenta**. No puede solicitar ni recibir préstamos hasta estar validada.

Las cuentas anteriores conservan su estado, fecha de validación y permisos. Si no tenían código ni revisor registrado, esos datos permanecen vacíos; una cuenta pendiente debe completar su código antes de validarse.

## Revisión en Personas

1. Selecciona **Pendientes de validar**. La vista también permite filtrar cuentas con correcciones pendientes, validadas o suspendidas.
2. Pulsa **Validar cuenta** directamente desde la lista o abre **Ver detalle** si necesitas consultar los datos.
3. Contrasta nombre, correo, código institucional y perfil con la documentación o información de la universidad. Si hace falta, el revisor autorizado puede completar los datos de una cuenta pendiente.
4. Pulsa **Validar cuenta** para confirmar la validación, sin casillas adicionales. Se guardan el identificador del validador y la fecha exacta usando el reloj del servidor. Validar no asigna permisos operativos ni responsabilidades.
5. Si hay problemas, abre **Corregir datos o pedir cambios** en el detalle, escribe observaciones y pulsa **Solicitar corrección**. La cuenta permanece pendiente; no se permite validarla mientras tenga una corrección sin reenviar.

Puede revisar un administrador validado o una cuenta validada con permiso `VALIDAR_USUARIO_PROVISIONAL`. No se permite la autovalidación. El backend exige confirmar la revisión y bloquea la fila durante la validación para que dos revisores no validen la misma cuenta simultáneamente.

## Corrección por la persona

La persona entra en **Mi cuenta**, lee las observaciones y modifica sus datos. Al pulsar **Guardar datos y enviar a revisión**, queda nuevamente pendiente de revisión administrativa. No puede modificar los datos de otra cuenta ni cambiar por esta vía los datos de una cuenta ya validada. El cambio de correo conserva su cuenta y credenciales; en futuros inicios de sesión utiliza el correo actualizado.

El revisor ve quién solicitó la corrección y cuándo. La última observación se conserva como referencia después del reenvío; la validación conserva por separado su responsable y fecha. Las observaciones y el código institucional solo se devuelven al titular o a revisores autorizados, no al público ni a otras personas sin ese acceso.

## Revisión de correo y acceso al buzón

Esta entrega comprueba los datos mediante **revisión administrativa**. No envía mensajes ni declara que el titular haya demostrado controlar su buzón. La verificación mediante un enlace de correo es una etapa independiente que requiere configurar el servicio de envío y su flujo de tokens.

## API

| Acción | Método y ruta |
|---|---|
| Registrar con `codigoInstitucional` | `POST /api/v1/usuarios` |
| Listar pendientes | `GET /api/v1/usuarios?estado=PROVISIONAL` |
| Validar con `datosRevisados: true` | `POST /api/v1/usuarios/{id}/validar` |
| Solicitar corrección con `observaciones` | `POST /api/v1/usuarios/{id}/solicitar-correccion` |
| Completar o reenviar datos pendientes | `PATCH /api/v1/usuarios/{id}/datos-pendientes` |

Las operaciones usan el actor del token. Cambiar el perfil solicitado no otorga permisos administrativos. La migración incremental V12 conserva las cuentas existentes y añade código, validador y campos de revisión.


## Navegación en Personas

La lista muestra perfil, estado, función y áreas. **Nueva persona** abre el registro solo cuando se necesita. **Validar cuenta** sigue disponible directamente en la fila. **Ver ficha** reúne los datos, las acciones de cuenta y el historial plegado. No se muestran ni editan permisos técnicos en esta pantalla. El administrador puede usar **Gestionar responsables** para abrir Administración o **Gestionar áreas** desde una ficha para abrir las asignaciones de esa persona. Las asignaciones mantienen su confirmación explícita y la limpieza del selector después de guardar.
