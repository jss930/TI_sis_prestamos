package pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio;

import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.ItemEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemJpaRepositorio extends JpaRepository<ItemEntidad, Long> {

    List<ItemEntidad> findByCategoria(ItemEntidad.CategoriaItem categoria);

    List<ItemEntidad> findByTituloContainingIgnoreCase(String titulo);

    @Query("SELECT i FROM ItemEntidad i WHERE i.cantidadDisponible > 0")
    List<ItemEntidad> findDisponibles();

    @Query("SELECT i FROM ItemEntidad i WHERE i.libro IS NOT NULL")
    List<ItemEntidad> findAllLibros();

    @Query("SELECT i FROM ItemEntidad i WHERE i.equipo IS NOT NULL")
    List<ItemEntidad> findAllEquipos();
}