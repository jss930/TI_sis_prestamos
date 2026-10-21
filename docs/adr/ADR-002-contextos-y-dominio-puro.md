# ADR-002 — Contextos delimitados y dominio puro

- **Estado:** aceptado
- **Fecha:** 2026-10-20
- **Contexto:** el sistema mezclaba catálogo, inventario, personas, préstamos y sanciones
  en un modelo anémico. Las reglas estaban dispersas en controladores y repositorios.

## Decisión

Organizar el backend en cinco contextos:

| Contexto | Responsabilidad |
|---|---|
| `catalog` | Obras/equipos y sus metadatos |
| `inventory` | Ejemplares físicos prestables |
| `identity` | Cuentas, estados y permisos operativos |
| `circulation` | Préstamos, devoluciones y renovaciones |
| `penalties` | Sanciones y multas |

Cada contexto tiene `domain`, `application` e infraestructura solo cuando la necesita.
El dominio no importa Spring, JPA, Hibernate, Jackson ni clases de `infrastructure`.
Los repositorios son puertos en `domain/repository/` y las políticas son clases Java sin
anotaciones. La inyección de dependencias se hace en infraestructura.

## Consecuencias

- Las reglas se prueban sin levantar Spring.
- ArchUnit impide que el dominio se contamine con el tiempo.
- Hay algo más de ceremonia (comandos, casos de uso, mapeos), justificada por la
  mantenibilidad y la sustentación.
- “Vencido” se deriva de fechas en lugar de ser un estado persistido, evitando jobs de
  actualización y estados inconsistentes.

## Alternativas descartadas

- **Un solo paquete por capa técnica** (`controller`, `service`, `repository`): vuelve a
  mezclar conceptos y no delimita responsabilidades.
- **Dominio con anotaciones JPA:** acopla el modelo al esquema y complica los tests.
