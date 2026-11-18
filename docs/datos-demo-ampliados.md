# Datos de demostración ampliados

## Cómo cargarlos

Reinicia el backend actualizado. El perfil `demo` (predeterminado en este
proyecto académico) agrega la colección `ampliada-v1` después de la carga base.
También funciona si la base ya contiene usuarios: no requiere borrar datos.

La migración V16 crea una fila de control. La carga es transaccional y bloquea
esa fila: se ejecuta una sola vez y los siguientes arranques no duplican ni
restauran registros que el usuario haya modificado. No se cambian usuarios,
contraseñas, bienes o préstamos existentes. Ante una colisión con un correo
o identificador reservado, se omite la carga y se informa en el log.

**No borres la base ni elimines la fila de control para volver a cargarla.**
Para un uso no académico, activa un perfil distinto de `demo`; estas cuentas
y contraseñas conocidas no son apropiadas para publicar en Internet.

## Nuevas cuentas

Todas usan `password`. Son cuentas ficticias identificadas con `DEMO-...`.

| Correo | Perfil / situación |
|---|---|
| `operador.demo@universidad.edu.pe` | Diego: administrador y operador autorizado |
| `maria.demo@universidad.edu.pe` | Alumna validada, préstamo vencido para practicar devolución |
| `jose.demo@universidad.edu.pe` | Alumno validado, reserva pendiente e historial |
| `lucia.demo@universidad.edu.pe` | Docente validada, libro a domicilio |
| `carlos.demo@universidad.edu.pe` | Administrativo validado, equipo a domicilio |
| `miguel.demo@universidad.edu.pe` | Docente validado, reserva aprobada |
| `provisional.demo@universidad.edu.pe` | Alumna pendiente de validación |
| `suspendida.demo@universidad.edu.pe` | Alumna suspendida con sanción activa |

Las cuatro cuentas originales siguen disponibles, sin cambiar su contraseña.
Elena tiene ejemplos como prestataria atendida por Diego, siempre que exista
la cuenta demo original. Si sus operaciones actuales no permiten otro activo,
ese ejemplo activo se asigna a José; nunca se evita una restricción.

## Colección

- Hasta nueve títulos nuevos: tres libros, tres equipos y tres materiales.
  Si un ISBN ya existe, se reutilizan sus metadatos sin modificarlos.
- Dieciocho ejemplares nuevos con códigos `DEMO-LIB-..`, `DEMO-EQU-..` y
  `DEMO-MAT-..`.
- Cuarenta préstamos: treinta y seis históricos (incluida una pérdida) y
  cuatro activos, uno de ellos vencido.
- Treinta y seis inspecciones, con devoluciones conformes, tardías, daño,
  pérdida y accesorios faltantes.
- Seis reservas: solicitada, aprobada, cancelada, denegada, expirada y atendida.
- Sanciones históricas cumplidas y una activa.

Las fechas se calculan respecto del primer arranque en Lima y después se
conservan. El paso del tiempo puede convertir activos en vencidos o expirar
reservas: reiniciar no cambia la historia.

## Recorrido sugerido

1. Entrar como Elena y abrir Reportes → General.
2. Ver los diez más recientes, pulsar Ver 10 más y filtrar por fecha/estado.
3. Imprimir sin cargar todas las páginas: el PDF debe incluir todos los
   resultados filtrados.
4. Consultar un ejemplar `DEMO-...` y revisar sus inspecciones.
5. Entrar como Diego para atender a Elena. No aprobar ni entregar acciones
   propias desde la misma cuenta.
6. Entrar como José, Lucía o Carlos y comparar Mi actividad con Atención.
7. Comprobar restricciones con las cuentas provisional y suspendida.
