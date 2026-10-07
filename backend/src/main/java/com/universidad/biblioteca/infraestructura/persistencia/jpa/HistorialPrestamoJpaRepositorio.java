package com.universidad.biblioteca.infraestructura.persistencia.jpa;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.HistorialPrestamoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialPrestamoJpaRepositorio extends JpaRepository<HistorialPrestamoEntidad, Long> {

    List<HistorialPrestamoEntidad> findByPersonaIdPersona(Long idPersona);

    List<HistorialPrestamoEntidad> findByPrestamoIdPrestamo(Long idPrestamo);

    List<HistorialPrestamoEntidad> findByPersonaIdPersonaOrderByFechaEventoDesc(Long idPersona);
}