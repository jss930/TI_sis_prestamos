package com.universidad.biblioteca.dominio.repositorios;

import pe.edu.unsa.sisprestamos.dominio.politicas_sanciones.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de Sanciones.
 */
public interface ISancionRepositorio {

    Sancion guardar(Sancion sancion);

    Optional<Sancion> buscarPorId(Long id);

    List<Sancion> buscarPorPersona(Long idPersona);

    List<Sancion> buscarPorPrestamo(Long idPrestamo);

    List<Sancion> buscarPorEstado(Sancion.Estado estado);

    List<Sancion> buscarActivasPorPersona(Long idPersona);

    List<Sancion> buscarVencidas(OffsetDateTime ahora);

    Optional<Sancion> buscarPorPrestamoYMotivo(Long idPrestamo, Sancion.Motivo motivo);
}