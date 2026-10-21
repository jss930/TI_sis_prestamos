# ADR-005 — Sanciones por evento de dominio

- **Estado:** aceptado
- **Fecha:** 2026-10-20
- **Contexto:** una devolución tardía debe generar una sanción. Si `circulation` llamara
  directamente a `penalties`, ambos contextos quedarían acoplados y la regla de préstamos
  dependería del modelo de sanciones.

## Decisión

`Prestamo` registra `PrestamoDevueltoConRetrasoEvent` cuando la devolución ocurre después
del vencimiento. `RegistrarDevolucionUseCase` publica los eventos a través del puerto
`DomainEventPublisher`. `PrestamoDevueltoConRetrasoListener` (en `penalties`) escucha el
evento, verifica que no exista una sanción para ese préstamo y crea la `Sancion`.

En el corte 1 la entrega es **síncrona, in-process**, con `@EventListener` de Spring. La
idempotencia se apoya en `SancionRepository.existePorPrestamo(prestamoId)`.

## Consecuencias

- `circulation` no importa nada de `penalties`.
- La demo es determinista: al responder la devolución, la sanción ya existe.
- Si el listener falla, la devolución falla; es aceptable para el corte 1, pero no para
  producción.
- En la fase 2 se migra a `@TransactionalEventListener` con patrón outbox, reintentos y
  consumidor idempotente. El dominio no cambia.

## Alternativas descartadas

- **Llamada directa `circulation → penalties`:** acopla contextos y mezcla transacciones.
- **`@Async` sin outbox:** pierde eventos si el proceso cae y hace la demo no determinista.
- **Broker desde el inicio:** infraestructura innecesaria para el alcance actual.
