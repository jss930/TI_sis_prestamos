package pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.PerfilEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilJpaRepositorio extends JpaRepository<PerfilEntidad, Long> {
}