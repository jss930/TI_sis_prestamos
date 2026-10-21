package com.universidad.biblioteca.infraestructura.persistencia.jpa;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.PoliticaPrestamoEntidad;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.ItemEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PoliticaPrestamoJpaRepositorio extends JpaRepository<PoliticaPrestamoEntidad, Long> {

    Optional<PoliticaPrestamoEntidad> findByTipoPersonaAndTipoItem(String tipoPersona, ItemEntidad.CategoriaItem tipoItem);

    List<PoliticaPrestamoEntidad> findByTipoPersona(String tipoPersona);
}