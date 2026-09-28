package pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.AdministrativoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdministrativoJpaRepositorio extends JpaRepository<AdministrativoEntidad, Long> {

    List<AdministrativoEntidad> findByArea(String area);
}