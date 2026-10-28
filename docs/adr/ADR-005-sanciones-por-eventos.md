# ADR-005: Sanciones por eventos de dominio

**Estado:** aceptado.

## Decisión

`Prestamo` registra `PrestamoDevueltoConRetrasoEvent` cuando corresponde. El publicador entrega el evento y el contexto de sanciones crea una sanción si todavía no existe una para ese préstamo.

## Consecuencias

- Circulación no depende del modelo interno de sanciones.
- El listener es idempotente.
- La publicación es síncrona y participa en la operación local.
