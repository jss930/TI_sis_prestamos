package com.universidad.biblioteca.infraestructura.persistencia.jpa;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.LibroEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibroJpaRepositorio extends JpaRepository<LibroEntidad, Long> {

    Optional<LibroEntidad> findByIsbn(String isbn);
}