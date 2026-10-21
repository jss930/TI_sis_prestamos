package com.universidad.biblioteca.dominio.repositorios;

import pe.edu.unsa.sisprestamos.dominio.circulacion.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de Préstamos.
 */
public interface IPrestamoRepositorio {

    Prestamo guardar(Prestamo prestamo);

    Optional<Prestamo> buscarPorId(Long id);

    List<Prestamo> buscarPorPersona(Long idPersona);

    List<Prestamo> buscarPorEjemplar(Long idEjemplar);

    List<Prestamo> buscarPorEstado(Prestamo.Estado estado);

    List<Prestamo> buscarActivosPorPersona(Long idPersona);

    List<Prestamo> buscarVencidos(OffsetDateTime ahora);

    Optional<Prestamo> buscarActivoPorEjemplar(Long idEjemplar);

    long contarPorPersonaYEstado(Long idPersona, Prestamo.Estado estado);
}