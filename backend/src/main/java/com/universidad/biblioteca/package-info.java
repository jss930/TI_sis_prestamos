/**
 * Capa de Repositorio - Implementación DDD
 * 
 * Esta capa implementa los puertos de salida (Output Ports) definidos en la capa de dominio.
 * Sigue el patrón Repository de DDD y utiliza Spring Data JPA como tecnología de persistencia.
 * 
 * Estructura:
 * - entidad/: Entidades JPA mapeadas a las tablas de PostgreSQL
 * - repositorio/: Interfaces Spring Data JPA (Spring Data Repositories)
 * - mapeador/: Mappers MapStruct para convertir entre entidades JPA y entidades de dominio
 * - impl/: Implementaciones concretas de los repositorios de dominio
 * - flyway/: Scripts de migración de base de datos
 * 
 * Principios aplicados:
 * - Los repositorios de dominio (IPersonaRepositorio, IItemRepositorio, etc.) son interfaces
 *   definidas en la capa de dominio (puertos de salida)
 * - Las implementaciones (PersonaRepositorioImpl, ItemRepositorioImpl, etc.) son adaptadores
 *   que usan Spring Data JPA internamente
 * - Los mappers convierten entre entidades JPA (infraestructura) y entidades de dominio
 * - Flyway gestiona la evolución del esquema de base de datos
 */
package com.universidad.biblioteca;