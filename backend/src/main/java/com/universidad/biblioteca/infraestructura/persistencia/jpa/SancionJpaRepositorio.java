package com.universidad.biblioteca.infraestructura.persistencia.jpa;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.SancionEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SancionJpaRepositorio extends JpaRepository<SancionEntidad, Long> {

    List<SancionEntidad> findByPersonaIdPersona(Long idPersona);

    List<SancionEntidad> findByPrestamoIdPrestamo(Long idPrestamo);

    List<SancionEntidad> findByEstado(SancionEntidad.EstadoSancion estado);

    @Query("SELECT s FROM SancionEntidad s WHERE s.persona.idPersona = :idPersona AND s.estado = 'ACTIVA'")
    List<SancionEntidad> findActivasByPersona(@Param("idPersona") Long idPersona);

    @Query("SELECT s FROM SancionEntidad s WHERE s.estado = 'ACTIVA' AND s.fechaFin < :ahora")
    List<SancionEntidad> findVencidas(@Param("ahora") OffsetDateTime ahora);

    Optional<SancionEntidad> findByPrestamoIdPrestamoAndMotivo(Long idPrestamo, SancionEntidad.MotivoSancion motivo);
}