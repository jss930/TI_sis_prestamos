# Plazos e historiales

## Regla de plazos

La misma política se aplica a préstamos y reservas. Los días son períodos exactos
de 24 horas, no días calendario redondeados al cierre.

| Perfil | Libro a domicilio | Equipo a domicilio | Otro |
|---|---|---|---|
| Alumno | 3 días | No permitido | Solo campus |
| Docente | 5 días | 3 días | Solo campus |
| Administrativo | 3 días | 3 días | Solo campus |

En campus, para cualquier perfil y bien: mínimo una hora, máximo trece horas,
siempre dentro del mismo día entre las 07:00 y las 20:00 en `America/Lima`.
A domicilio: mínimo una hora y máximo según la tabla; la entrega y devolución
también deben ocurrir dentro del horario de atención.

La reserva conserva su anticipación mínima de 30 minutos. Una recogida tardía
no extiende su vencimiento ni permite entregar con menos de una hora restante.
Una renovación no puede superar el límite total desde la entrega original.

## Consultas protegidas

- `GET /api/v1/prestamos`, detalle y renovación exigen un token válido.
- Sin permisos operativos, el listado devuelve únicamente préstamos propios.
  Consultar o renovar un préstamo ajeno se rechaza con HTTP 403.
- Personal validado con permiso de registro o devolución y administradores
  pueden consultar préstamos para atender ventanilla.
- Las sanciones solo las consulta su titular, un gestor de sanciones o el
  administrador. Su escritura obtiene al operador del token, no del cuerpo.
- Reservas exige autenticación también para listar.
- Cerrar sesión elimina los datos privados de la interfaz e invalida cargas
  anteriores; el catálogo público no solicita estas rutas protegidas.
- La API de renovación admite `{"nuevoVencimiento":"...ISO-8601..."}`.
  Si no se especifica, intenta ampliar una hora, siempre sujeta al máximo total.

## Formularios y acciones

Los formularios expresan todas las fechas en hora de Lima, incluso si el
navegador está configurado en otra zona. Ocultan domicilio cuando no corresponde,
recalculan el vencimiento al cambiar de persona/bien/lugar y validan los mismos
límites antes de enviar. El backend vuelve a validar: la interfaz no concede permisos.

La aprobación comprueba la disponibilidad futura en el servidor. No se desactiva
solo porque una copia esté prestada actualmente. La entrega exige que haya
comenzado la reserva, que no haya expirado la recogida y que quede al menos una
hora de uso. La renovación solo aparece cuando queda margen dentro del máximo
total; no convierte un préstamo de campus en uno de varios días.

`cd frontend && pnpm test` ejecuta las pruebas de matriz, límites y conversión
horaria. `pnpm build` también las ejecuta antes de TypeScript/Vite, por lo que
forman parte del CI existente sin modificar su workflow.

## Historial y reporte

- **Mi actividad → Mi historial y reporte** para cuentas
  personales, y **Reportes** para administradores (General, Por usuario, Por ejemplar).
- Administrador: elige usuario o ejemplar en esa pantalla. También puede entrar
  desde **Personas → Ver historial**, o desde **Gestión del catálogo →
  Historial de ejemplares físicos**.
- **Aplicar filtros**: rango de fechas inclusivo en Lima y estado del préstamo.
  Las fechas corresponden a entrega, inicio reservado y alta de sanción,
  respectivamente. El estado solo filtra préstamos.
- **Imprimir / guardar PDF** abre el diálogo del navegador y conserva cabecera,
  período, indicadores y tablas, ocultando navegación y controles.

API: `GET /api/v1/reportes/usuarios/{id}` (titular o administrador) y
`GET /api/v1/reportes/ejemplares/{id}` (solo administrador). Parámetros opcionales:
`desde=YYYY-MM-DD`, `hasta=YYYY-MM-DD`, `estado=ACTIVO|DEVUELTO|PERDIDO`.

La API admite además `pagina`, `tamano`, `busqueda` y `completo`.
La interfaz muestra diez registros por sección y acumula Ver 10 más.
Imprimir solicita explícitamente `completo=true`, conservando el mismo filtro.
Los indicadores no se calculan solo con la página visible.

Se muestran préstamos, renovaciones acumuladas, entrega/vencimiento/devolución
real, inspecciones, reservas y sanciones. El retraso del reporte usa instantes
exactos, no solo fechas. Una inspección ausente se informa como no registrada.
No se intenta reconstruir la fecha de cada renovación ni cambios de inventario
que el sistema no guardó. Son reportes operativos, no una auditoría completa.

La detección de préstamos vencidos también usa la hora exacta en consultas y
elegibilidad: un préstamo de campus vencido hace una hora bloquea una nueva
operación, aunque todavía sea el mismo día. El contador de días muestra días
completos; antes de un día se informa que el horario de devolución venció.

Validación de interfaz: comprobar las nueve combinaciones de perfil/bien,
cambiar la zona del navegador, registrar un préstamo, revisar acciones de
reservas y abrir/imprimir un historial. Los formularios y el reporte deben
conservar la hora de Lima y ocultar los controles de impresión.
