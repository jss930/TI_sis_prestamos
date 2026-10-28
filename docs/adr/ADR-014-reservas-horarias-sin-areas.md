# ADR-014: Reservas horarias por título

**Estado:** aceptado.

## Decisión

Una reserva guarda título, inicio, fin y lugar de uso. Exige dos horas de anticipación, jornada de 07:00 a 20:00 y duración máxima de 24 horas.

Solo el administrador aprueba manualmente. El sistema asigna una copia libre para el intervalo. La recogida se permite desde el inicio hasta una hora después. Luego expira sin sanción.

No existe lista de espera ni una entidad separada de apartado.

## Consecuencias

- Una copia puede atender intervalos consecutivos.
- La aprobación usa bloqueo pesimista y segunda comprobación.
- Los préstamos directos respetan reservas aprobadas.
- Los permisos se administran por cuenta.
