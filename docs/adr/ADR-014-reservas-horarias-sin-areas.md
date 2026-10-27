# ADR-014: Reservas horarias por título y administración directa

Estado: aceptado.

## Contexto

El modelo anterior apartaba una copia durante 48 horas y autorizaba por responsables de área. El proceso académico requiere reservar un título para un intervalo futuro, permitir horarios no superpuestos y simplificar la administración.

## Decisión

La reserva guarda inicio y fin, con anticipación mínima de dos horas, jornada de 07:00 a 20:00 y duración máxima de 24 horas. Solo el administrador aprueba manualmente. La copia se asigna al aprobar, pero no cambia a estado físico `RESERVADO`; el conflicto se calcula por intersección de intervalos.

La recogida se permite desde el inicio hasta una hora después. Luego expira sin sanción. La reprogramación solo corresponde al titular, a reservas pendientes y hasta una hora antes del inicio original.

Se retiran de la API y del frontend las áreas, responsables y capacitación. Los permisos se administran directamente por cuenta. Las migraciones publicadas se conservan por la inmutabilidad de Flyway.

## Consecuencias

Una misma copia puede atender reservas consecutivas. Aprobar requiere bloqueo pesimista y una segunda comprobación de solapamiento. Los préstamos directos consultan un puerto de disponibilidad para no invadir horarios aprobados. El modelo es más simple para el usuario y mantiene aislado el contexto de circulación.
