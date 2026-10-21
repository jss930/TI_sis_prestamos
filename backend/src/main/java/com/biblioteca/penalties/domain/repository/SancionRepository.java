package com.biblioteca.penalties.domain.repository;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.vo.SancionId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto de salida del contexto penalties. */
public interface SancionRepository {

    Sancion guardar(Sancion sancion);

    Optional<Sancion> buscarPorId(SancionId id);

    List<Sancion> listarTodas();

    List<Sancion> listarPorUsuario(UsuarioId usuarioId);

    boolean existeActivaPara(UsuarioId usuarioId, LocalDate fecha);

    boolean existePorPrestamo(PrestamoId prestamoId);
}
