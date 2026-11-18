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
