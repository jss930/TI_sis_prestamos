# ADR-008 — Reservas fuera del alcance del corte 1

- **Estado:** aceptado
- **Fecha:** 2026-10-20
- **Contexto:** el hito del 50 % debía priorizar la solidez del núcleo: persistencia real,
  autenticación JWT, préstamo directo en ventanilla e integración de un frontend básico.
  Las reservas agregan un flujo con expiración, colas por ítem y conversión a préstamo que
  no es necesario para demostrar las reglas actuales.

## Decisión

Dejar las reservas **fuera del alcance principal** del corte 1 y del siguiente hito
inmediato. No se crean endpoints, tablas ni agregados de reservas todavía. El diseño se
documenta como extensión futura.

## Consecuencias

- El sistema actual no promete reservas ni colas de espera.
- No se alteran las reglas de préstamo ni la base de datos existente.
- `circulation` queda preparado para incorporar el flujo:
  - una reserva sería un agregado propio (`Reserva`) con `EjemplarId`, `UsuarioId`,
    estado (`PENDIENTE`, `CONFIRMADA`, `EXPIRADA`, `CANCELADA`, `CONVERTIDA`) y fechas;
  - la conversión a préstamo reutilizaría `RegistrarPrestamoUseCase` y sus invariantes;
  - la expiración se resolvería con una política y un job/evento, no dentro de `Prestamo`.
- Cuando se implementen, se evaluará si el contexto dueño es `circulation` o un nuevo
  `reservations`; no se fuerza la decisión ahora.

## Alternativas descartadas

- **Incluirlas en el corte 1:** habría reducido el tiempo para persistencia y seguridad,
  que son requisitos estructurales.
- **Modelarlas como un estado de `Prestamo`:** mezcla dos ciclos de vida y complica las
  reglas de disponibilidad. Es preferible un agregado separado.
