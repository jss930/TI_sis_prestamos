package pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.AlumnoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlumnoJpaRepositorio extends JpaRepository<AlumnoEntidad, Long> {

    Optional<AlumnoEntidad> findByCui(String cui);
}