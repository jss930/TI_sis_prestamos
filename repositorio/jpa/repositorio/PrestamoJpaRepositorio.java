package pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.PrestamoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoJpaRepositorio extends JpaRepository<PrestamoEntidad, Long> {

    List<PrestamoEntidad> findByPersonaIdPersona(Long idPersona);

    List<PrestamoEntidad> findByEjemplarIdEjemplar(Long idEjemplar);

    List<PrestamoEntidad> findByEstado(PrestamoEntidad.EstadoPrestamo estado);

    @Query("SELECT p FROM PrestamoEntidad p WHERE p.persona.idPersona = :idPersona AND p.estado = 'ACTIVO'")
    List<PrestamoEntidad> findActivosByPersona(@Param("idPersona") Long idPersona);

    @Query("SELECT p FROM PrestamoEntidad p WHERE p.estado = 'ACTIVO' AND p.fechaVencimiento < :ahora")
    List<PrestamoEntidad> findVencidos(@Param("ahora") OffsetDateTime ahora);

    @Query("SELECT p FROM PrestamoEntidad p WHERE p.ejemplar.idEjemplar = :idEjemplar AND p.estado = 'ACTIVO'")
    Optional<PrestamoEntidad> findActivoByEjemplar(@Param("idEjemplar") Long idEjemplar);

    long countByPersonaIdPersonaAndEstado(Long idPersona, PrestamoEntidad.EstadoPrestamo estado);
}