package pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.EquipoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipoJpaRepositorio extends JpaRepository<EquipoEntidad, Long> {

    Optional<EquipoEntidad> findByNumeroSerie(String numeroSerie);
}