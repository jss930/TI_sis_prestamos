package pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.ReservaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaJpaRepositorio extends JpaRepository<ReservaEntidad, Long> {

    List<ReservaEntidad> findByPersonaIdPersona(Long idPersona);

    List<ReservaEntidad> findByItemIdItem(Long idItem);

    List<ReservaEntidad> findByEstado(ReservaEntidad.EstadoReserva estado);

    @Query("SELECT r FROM ReservaEntidad r WHERE r.item.idItem = :idItem AND r.estado IN ('PENDIENTE', 'CONFIRMADA') ORDER BY r.fechaReserva ASC")
    List<ReservaEntidad> findPendientesByItem(@Param("idItem") Long idItem);

    @Query("SELECT r FROM ReservaEntidad r WHERE r.estado = 'PENDIENTE' AND r.fechaExpiracion < :ahora")
    List<ReservaEntidad> findExpiradas(@Param("ahora") OffsetDateTime ahora);

    @Query("SELECT r FROM ReservaEntidad r WHERE r.ejemplar.idEjemplar = :idEjemplar AND r.estado IN ('PENDIENTE', 'CONFIRMADA')")
    Optional<ReservaEntidad> findActivaByEjemplar(@Param("idEjemplar") Long idEjemplar);

    long countByPersonaIdPersonaAndEstado(Long idPersona, ReservaEntidad.EstadoReserva estado);
}