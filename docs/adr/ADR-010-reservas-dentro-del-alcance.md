# ADR-010 — Reservas dentro del alcance

- **Estado:** aceptado
- **Fecha:** 2026-10-21
- **Contexto:** ADR-008 dejaba las reservas fuera del alcance del corte 1. La
  nueva entrega incorpora un contexto delimitado `reservations` con el agregado
  `Reserva`, comunicandose con `circulation` mediante el puerto de aplicacion
  `RegistrarPrestamoPort`.

## Decisión

Las reservas quedan dentro del alcance. Se crea el contexto `reservations`,
con estados `SOLICITADA`, `ACEPTADA`, `DENEGADA` y `CANCELADA`, persistencia
propia (`V2__reservas.sql`) y endpoints `/api/v1/reservas`.

## Consecuencias

- ADR-008 queda superado.
- `reservations` no importa el dominio de `circulation`; la conversion a
  prestamo se delega en infraestructura.
- Se agrega el permiso `GESTIONAR_RESERVAS`.

## Alternativas descartadas

- Mantener las reservas fuera del alcance: contradice el nuevo requisito.
