# Guía completa del sistema desde nivel cero

**Universidad Nacional de San Agustín de Arequipa**<br>
**Escuela Profesional de Ciencia de la Computación**<br>
**Trabajo Interdisciplinario II — Grupo 3**

## 1. El problema que resuelve

Una universidad dispone de libros, equipos y otros bienes. Necesita saber:

- qué recursos existen;
- cuántas copias físicas hay;
- quién puede pedirlas;
- cuándo estarán ocupadas;
- quién entregó y recibió cada copia;
- cuándo deben devolverse;
- si una persona tiene una sanción.

El sistema organiza esas tareas y evita conflictos, por ejemplo, entregar una laptop que ya está reservada para el mismo horario.

## 2. Las seis piezas

### Catálogo

Es la ficha general. “Clean Code” aparece una sola vez aunque existan cinco copias.

### Inventario

Contiene las copias físicas. Cada copia tiene UUID, código de barras, ubicación y estado.

### Cuenta

Representa a la persona. Su perfil determina plazos y límites. Sus permisos determinan qué tareas administrativas puede ejecutar.

### Reserva

Representa la intención de utilizar un título en el futuro. Guarda inicio, fin, lugar de uso, estado y copia asignada al aprobar.

### Préstamo

Representa la entrega física. Guarda quién operó, quién recibió, qué copia salió y cuándo debe regresar.

### Sanción

Representa una restricción activa por retraso, daño, pérdida o decisión administrativa.

## 3. Historia completa de un usuario

### Registro

1. La persona registra nombre, correo, código institucional, perfil y contraseña.
2. La cuenta queda provisional.
3. Puede entrar, ver el catálogo y revisar su cuenta.
4. No puede reservar todavía.

### Validación

1. Una cuenta autorizada revisa los datos.
2. Puede solicitar correcciones.
3. Cuando todo es correcto, valida la cuenta.
4. La persona ya puede reservar y recibir préstamos.

### Reserva

1. Abre el catálogo.
2. Selecciona un título.
3. Elige fecha y hora de inicio y fin.
4. Si es libro, elige campus o domicilio.
5. Confirma la solicitud.
6. El administrador la revisa.
7. Al aprobar, el sistema asigna una copia libre.

### Recogida

1. La persona llega desde la hora inicial.
2. Tiene una hora de tolerancia.
3. El administrador confirma la entrega.
4. Se crea un préstamo y la reserva queda atendida.

### Devolución

1. El administrador localiza el préstamo.
2. Inspecciona el estado físico.
3. Registra devolución, daño, pérdida o revisión.
4. El préstamo se cierra y el ejemplar cambia de estado.
5. Un retraso puede producir una sanción.

## 4. Reglas fáciles de recordar

```text
2 horas   anticipación mínima
24 horas  duración máxima de reserva
07–20     horario permitido
1 hora    tolerancia para recoger
0 colas   no existe lista de espera
1 título  el usuario nunca elige copia
```

## 5. Quién hace cada tarea

| Tarea | Usuario provisional | Usuario validado | Administrador |
|---|:---:|:---:|:---:|
| Ver catálogo | Sí | Sí | Sí |
| Ver su cuenta | Sí | Sí | Sí |
| Crear reserva | No | Sí | Sí |
| Cancelar su reserva | No | Sí | Sí |
| Aprobar o denegar | No | No | Sí |
| Confirmar entrega reservada | No | No | Sí |
| Validar cuentas | No | Con permiso | Sí |
| Gestionar catálogo | No | Con permiso | Sí |
| Registrar préstamo/devolución | No | Con permiso | Sí |

## 6. Cómo viaja la información

```text
Persona usa el navegador
        │
        ▼
React valida y envía JSON
        │
        ▼
Spring recibe la petición
        │
        ▼
Caso de uso coordina la operación
        │
        ▼
Dominio verifica las reglas
        │
        ▼
JPA traduce objetos a filas
        │
        ▼
PostgreSQL confirma la transacción
        │
        ▼
La respuesta vuelve a la pantalla
```

## 7. Ejemplo de reserva en código

| Paso | Archivo |
|---|---|
| Pantalla | `frontend/src/pages/ReservaNueva.tsx` |
| Cliente API | `frontend/src/services/api.ts` |
| Entrada HTTP | `ReservaController.java` |
| Coordinación | `SolicitarReservaUseCase.java` |
| Reglas horarias | `PoliticaHorarioReserva.java` |
| Agregado | `Reserva.java` |
| Puerto | `ReservaRepository.java` |
| Adaptador | `ReservaJpaAdapter.java` |
| Tabla | `reservas` |

## 8. Qué significa DDD aquí

DDD no significa usar nombres complicados. Significa que el código usa las mismas palabras del negocio y protege sus reglas.

```text
Negocio dice “una reserva expira”
Código tiene Reserva.expirar()

Negocio dice “no se puede recoger antes”
Código tiene Reserva.exigirRetiroVigente()

Negocio dice “una copia no puede cruzar horarios”
Código tiene DisponibilidadProgramadaPort
```

## 9. Qué significa arquitectura hexagonal

El centro no conoce la tecnología exterior:

```text
Dominio define ReservaRepository
Infraestructura implementa ReservaJpaAdapter
```

Por eso un test puede usar un repositorio en memoria y la aplicación real usar PostgreSQL sin cambiar las reglas.

## 10. Qué significa transacción

Una transacción es “todo o nada”. Al entregar una reserva se deben crear el préstamo, actualizar el ejemplar y actualizar la reserva. Si una parte falla, se deshace toda la operación para no dejar datos contradictorios.

## 11. Qué significa concurrencia

Concurrencia significa que dos acciones ocurren casi al mismo tiempo. Si dos administradores quieren la última copia, ambos podrían verla libre. El bloqueo pesimista hace que uno espere; luego vuelve a comprobar y evita la doble asignación.

## 12. Qué estudiar después

1. Lee [`solicitudes-prestamo.md`](solicitudes-prestamo.md).
2. Lee [`arquitectura.md`](arquitectura.md).
3. Ejecuta las consultas de [`base-de-datos.md`](base-de-datos.md).
4. Sigue la demo de [`demo.md`](demo.md).
5. Practica con [`guia-sustentacion.md`](guia-sustentacion.md).
