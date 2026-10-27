# ADR-011: Separar la aprobación de la entrega física

Estado: aceptado.

## Contexto

Aceptar una reserva creaba inmediatamente un préstamo. Las responsabilidades por área habilitaban ventanilla, pero no permitían atender solicitudes de forma restringida por categoría. Era necesario evitar el inicio del plazo antes de la recogida y los apartados duplicados.

## Decisión

Se amplía el contexto `reservations` para representar solicitudes inmediatas, conservando sus rutas existentes. La aprobación asigna una unidad en estado `RESERVADO` y un plazo de recogida de 48 horas. La entrega crea el préstamo mediante el caso de uso de circulación y pasa la solicitud a `ATENDIDA`. El estado anterior `ACEPTADA` conserva su significado histórico.

El área es un atributo explícito de catálogo independiente de su tipo. Una política de solicitudes cruza el área con las asignaciones vigentes de identidad; catálogo e identidad permanecen independientes. Las lecturas se restringen a solicitudes propias o de áreas autorizadas.

Aprobación, entrega, cancelación y vencimiento bloquean la solicitud y el ejemplar dentro de una transacción. Las operaciones que asignan una unidad también bloquean al prestatario antes del ejemplar para serializar sus límites; el préstamo directo comparte el bloqueo de unidad. La entrega revierte la liberación del apartado si la política de circulación falla. Un proceso periódico libera apartados vencidos; la entrega comprueba el plazo en cada petición.

## Consecuencias

Aprobación y entrega tienen responsables y fechas separados. No se incorporan reservas para fechas futuras ni nuevas dependencias. Las condiciones del préstamo se reevalúan al recoger el artículo. Un reinicio del backend es necesario para aplicar la migración incremental V10. Se mantiene el flujo existente de devolución e inspección.
