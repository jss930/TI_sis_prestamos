package com.universidad.biblioteca.infraestructura.persistencia.jpa;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.UbicacionFisicaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UbicacionFisicaJpaRepositorio extends JpaRepository<UbicacionFisicaEntidad, Long> {
}