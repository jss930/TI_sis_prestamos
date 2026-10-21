# ADR-004 — Persistencia in-memory primero, JPA después

- **Estado:** aceptado (decisión temporal)
- **Fecha:** 2026-10-20
- **Contexto:** el corte 1 debía quedar ejecutable y demostrable hoy, sin depender de
  PostgreSQL, Docker ni credenciales. El modelo todavía se está validando.

## Decisión

Implementar adaptadores in-memory para los cinco puertos de repositorio en
`infrastructure/persistence/inmemory/`. La aplicación arranca con el perfil `demo` y
carga datos fijos. No se añade JPA, Flyway ni Testcontainers en este corte.

## Consecuencias

- `mvn clean test` y `spring-boot:run` funcionan sin infraestructura externa.
- Los tests de aplicación usan adaptadores reales, no mocks de repositorio.
- Los datos se pierden al reiniciar.
- No se prueba concurrencia, transacciones ni restricciones de base de datos.
- La fase 2 sustituye los adaptadores sin tocar dominio, casos de uso ni controladores.

## Plan de salida

1. Añadir `spring-boot-starter-data-jpa`, PostgreSQL y Flyway.
2. Mapear agregados a entidades JPA y value objects a `@Embeddable`.
3. Crear índices únicos: `ejemplar.barcode`, préstamo activo por ejemplar, sanción por
   usuario/estado/fechas.
4. Migrar los seeds a `db/migration` o a un `ApplicationRunner` de perfil `demo`.
5. Añadir tests de integración con Testcontainers.

## Alternativas descartadas

- **JPA desde el día 1:** habría retrasado la demo y acoplado el modelo antes de validarlo.
- **H2:** se parece a SQL, pero no reproduce particularidades de PostgreSQL ni migraciones
  reales; se prefiere Testcontainers en la fase 2.
