/**
 * Entidades JPA - Mapeo Objeto-Relacional
 * 
 * Estas entidades representan el mapeo directo a las tablas de PostgreSQL.
 * Utilizan anotaciones de Jakarta Persistence (JPA) y Lombok para reducir boilerplate.
 * 
 * Características:
 * - Herencia JOINED para Item -> Libro/Equipo
 * - Relaciones bidireccionales con mappedBy
 * - Enums mapeados como STRING para legibilidad
 * - JSONB para permisos de administrador
 * - Índices definidos en @Table para optimización de consultas
 * - Validaciones Bean Validation (@NotNull, @Size, etc.)
 * - Callbacks @PrePersist/@PreUpdate para auditoría
 */
package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;