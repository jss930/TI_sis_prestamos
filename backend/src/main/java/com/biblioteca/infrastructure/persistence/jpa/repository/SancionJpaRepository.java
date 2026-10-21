package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.SancionJpaEntity;
import com.biblioteca.penalties.domain.model.EstadoSancion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SancionJpaRepository extends JpaRepository<SancionJpaEntity, UUID> {

    List<SancionJpaEntity> findByUsuarioId(UUID usuarioId);

    boolean existsByUsuarioIdAndEstadoAndFechaInicioLessThanEqualAndFechaFinGreaterThan(
            UUID usuarioId, EstadoSancion estado, LocalDate fechaInicio, LocalDate fechaFin);

    boolean existsByPrestamoId(UUID prestamoId);
}
