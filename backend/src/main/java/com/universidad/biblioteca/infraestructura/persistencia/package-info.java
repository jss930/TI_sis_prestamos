/**
 * Implementaciones de Repositorios de Dominio (Adaptadores de Salida)
 * 
 * Estas clases implementan las interfaces definidas en la capa de dominio
 * (puertos de salida) y delegan en los repositorios Spring Data JPA.
 * 
 * Responsabilidades:
 * - Orquestar guardado de agregados complejos (Persona + Admin/Alumno/Docente/Administrativo)
 * - Convertir entre entidades de dominio y JPA usando mappers
 * - Manejar transacciones (@Transactional)
 * - Encapsular detalles de persistencia
 */
package com.universidad.biblioteca.infraestructura.persistencia;