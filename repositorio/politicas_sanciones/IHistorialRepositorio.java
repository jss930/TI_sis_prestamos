package pe.edu.unsa.sisprestamos.repositorio.politicas_sanciones;

import pe.edu.unsa.sisprestamos.dominio.politicas_sanciones.*;
import java.util.List;

/**
 * Interfaz del repositorio de Historial de Préstamos.
 */
public interface IHistorialRepositorio {

    HistorialPrestamo guardar(HistorialPrestamo historial);

    List<HistorialPrestamo> buscarPorPersona(Long idPersona);

    List<HistorialPrestamo> buscarPorPrestamo(Long idPrestamo);

    List<HistorialPrestamo> buscarPorPersonaOrdenadoPorFechaDesc(Long idPersona);
}