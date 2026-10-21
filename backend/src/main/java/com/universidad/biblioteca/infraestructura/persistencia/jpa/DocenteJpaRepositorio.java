package com.universidad.biblioteca.infraestructura.persistencia.jpa;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.DocenteEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocenteJpaRepositorio extends JpaRepository<DocenteEntidad, Long> {

    List<DocenteEntidad> findByDepartamento(String departamento);
}