package com.universidad.biblioteca.dominio.repositorios;

import pe.edu.unsa.sisprestamos.dominio.circulacion.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de Reservas.
 */
public interface IReservaRepositorio {

    Reserva guardar(Reserva reserva);

    Optional<Reserva> buscarPorId(Long id);

    List<Reserva> buscarPorPersona(Long idPersona);

    List<Reserva> buscarPorItem(Long idItem);

    List<Reserva> buscarPorEstado(Reserva.Estado estado);

    List<Reserva> buscarPendientesPorItem(Long idItem);

    List<Reserva> buscarExpiradas(OffsetDateTime ahora);

    Optional<Reserva> buscarActivaPorEjemplar(Long idEjemplar);

    long contarPorPersonaYEstado(Long idPersona, Reserva.Estado estado);
}