package com.biblioteca.circulation.domain.repository;

import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Puerto de salida del contexto circulation. */
public interface PrestamoRepository {

    Prestamo guardar(Prestamo prestamo);

    Optional<Prestamo> buscarPorId(PrestamoId id);

    default Optional<Prestamo> buscarPorIdParaActualizar(PrestamoId id) {
        return buscarPorId(id);
    }

    List<Prestamo> listarTodos();

    List<Prestamo> listarPorPrestatario(UsuarioId prestatarioId);

    boolean existeActivoPara(EjemplarId ejemplarId);

    long contarActivosPor(UsuarioId prestatarioId);

    boolean tieneVencido(UsuarioId prestatarioId, LocalDate hoy);

    default boolean existeSolapamiento(EjemplarId ejemplarId, Instant inicio, Instant fin) {
        return listarTodos().stream().anyMatch(prestamo ->
                prestamo.estado() == com.biblioteca.circulation.domain.model.EstadoPrestamo.ACTIVO
                && prestamo.ejemplarId().equals(ejemplarId)
                && prestamo.fechaHoraEntrega().isBefore(fin)
                && prestamo.fechaHoraVencimiento().isAfter(inicio));
    }
}
