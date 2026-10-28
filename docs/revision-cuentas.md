# Registro y revisión de cuentas

## 1. Registro

La persona indica:

- nombre completo;
- correo institucional;
- código universitario o de personal;
- perfil: alumno, docente o administrativo;
- contraseña de al menos ocho caracteres.

El código acepta letras, números y guiones, se normaliza en mayúsculas y no puede repetirse.

## 2. Estado provisional

Una cuenta nueva queda `PROVISIONAL`, mostrado como “Pendiente de validación”. Puede:

- iniciar sesión;
- consultar catálogo;
- abrir Mi cuenta;
- corregir datos cuando la administración lo solicita.

No puede reservar ni recibir préstamos.

## 3. Revisión administrativa

Una cuenta validada con `VALIDAR_USUARIO_PROVISIONAL` o `ADMINISTRAR_SISTEMA` puede revisar.

1. Abre Personas.
2. Filtra cuentas pendientes.
3. Revisa nombre, correo, código y perfil.
4. Valida o solicita una corrección.

No se permite revisar la propia cuenta. La fila se bloquea durante la operación para evitar dos validaciones simultáneas.

## 4. Corrección

Si hay un dato incorrecto:

1. el revisor escribe una observación;
2. la cuenta conserva estado provisional;
3. la persona abre Mi cuenta;
4. corrige y reenvía;
5. la administración revisa nuevamente.

Mientras exista una corrección pendiente, la cuenta no puede validarse.

## 5. Validación

Al validar se guardan:

- estado `VALIDADO`;
- fecha y hora;
- identificador de quien validó.

Validar habilita reservas y préstamos, pero no concede permisos administrativos.

## 6. Suspensión y reactivación

Una cuenta validada puede suspenderse con motivo. Una cuenta suspendida no reserva ni recibe préstamos. La reactivación devuelve el estado `VALIDADO`.

## 7. Permisos

Los permisos se conceden directamente a una cuenta validada:

- administración general;
- préstamos;
- devoluciones;
- validación de cuentas;
- catálogo;
- sanciones;
- usuarios.

## 8. Privacidad

El código institucional y los datos de revisión se muestran solo al titular o a cuentas autorizadas. La autenticación comprueba contraseña, pero no envía un enlace de verificación al correo.

## 9. API

| Acción | Ruta |
|---|---|
| Registrar | `POST /api/v1/usuarios` |
| Listar pendientes | `GET /api/v1/usuarios?estado=PROVISIONAL` |
| Validar | `POST /api/v1/usuarios/{id}/validar` |
| Solicitar corrección | `POST /api/v1/usuarios/{id}/solicitar-correccion` |
| Reenviar datos | `PATCH /api/v1/usuarios/{id}/datos-pendientes` |
| Suspender | `POST /api/v1/usuarios/{id}/suspender` |
| Reactivar | `POST /api/v1/usuarios/{id}/reactivar` |
| Conceder permiso | `POST /api/v1/usuarios/{id}/permisos` |
| Revocar permiso | `DELETE /api/v1/usuarios/{id}/permisos/{permiso}` |
