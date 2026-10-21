package com.universidad.biblioteca.infraestructura.persistencia.jpa;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.PersonaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaJpaRepositorio extends JpaRepository<PersonaEntidad, Long> {

    Optional<PersonaEntidad> findByContactoCorreo(String correo);

    boolean existsByContactoCorreo(String correo);

    List<PersonaEntidad> findByEstado(PersonaEntidad.EstadoPersona estado);

    List<PersonaEntidad> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(String nombres, String apellidos);

    @Query("SELECT p FROM PersonaEntidad p WHERE p.admin IS NOT NULL")
    List<PersonaEntidad> findAllAdmins();

    @Query("SELECT p FROM PersonaEntidad p WHERE p.alumno IS NOT NULL")
    List<PersonaEntidad> findAllAlumnos();

    @Query("SELECT p FROM PersonaEntidad p WHERE p.docente IS NOT NULL")
    List<PersonaEntidad> findAllDocentes();

    @Query("SELECT p FROM PersonaEntidad p WHERE p.administrativo IS NOT NULL")
    List<PersonaEntidad> findAllAdministrativos();

    @Query("SELECT p FROM PersonaEntidad p WHERE p.perfil.prestamoDomicilioEquipos = true")
    List<PersonaEntidad> findByPermitePrestamoDomicilioEquipos();
}