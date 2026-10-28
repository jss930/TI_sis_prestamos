# ADR-002: Contextos delimitados y dominio puro

**Estado:** aceptado.

## Decisión

El backend se organiza en seis contextos: catálogo, inventario, identidad, reservas, circulación y sanciones.

Cada contexto contiene dominio y aplicación. El dominio no importa Spring, JPA, Hibernate, Jackson ni infraestructura. Los repositorios son puertos y los adaptadores implementan la tecnología.

## Consecuencias

- Las reglas se prueban sin HTTP ni base real.
- ArchUnit protege las dependencias.
- El código tiene más clases pequeñas, pero cada una posee una responsabilidad clara.
