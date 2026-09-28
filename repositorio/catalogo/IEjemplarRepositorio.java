package pe.edu.unsa.sisprestamos.repositorio.catalogo;

import pe.edu.unsa.sisprestamos.dominio.catalogo.*;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de Ejemplares.
 */
public interface IEjemplarRepositorio {

    Ejemplar guardar(Ejemplar ejemplar);

    Optional<Ejemplar> buscarPorId(Long id);

    Optional<Ejemplar> buscarPorCodigoInventario(String codigoInventario);

    List<Ejemplar> buscarPorItem(Long idItem);

    List<Ejemplar> buscarDisponiblesPorItem(Long idItem);

    List<Ejemplar> buscarPorEstadoFisico(Ejemplar.EstadoFisico estadoFisico);

    void eliminarPorId(Long id);
}