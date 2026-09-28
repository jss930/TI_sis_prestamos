package pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.EjemplarEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EjemplarJpaRepositorio extends JpaRepository<EjemplarEntidad, Long> {

    Optional<EjemplarEntidad> findByCodigoInventario(String codigoInventario);

    List<EjemplarEntidad> findByItemIdItem(Long idItem);

    List<EjemplarEntidad> findByItemIdItemAndDisponibleTrue(Long idItem);

    @Query("SELECT e FROM EjemplarEntidad e WHERE e.item.idItem = :idItem AND e.estadoFisico = 'DISPONIBLE' AND e.disponible = true")
    List<EjemplarEntidad> findDisponiblesByItem(@Param("idItem") Long idItem);

    List<EjemplarEntidad> findByEstadoFisico(EjemplarEntidad.EstadoFisico estadoFisico);
}