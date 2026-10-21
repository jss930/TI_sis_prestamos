package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.infrastructure.persistence.jpa.entity.PrestamoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PrestamoJpaRepository extends JpaRepository<PrestamoJpaEntity, UUID> {

    List<PrestamoJpaEntity> findByPrestatarioId(UUID prestatarioId);

    boolean existsByEjemplarIdAndEstado(UUID ejemplarId, EstadoPrestamo estado);

    long countByPrestatarioIdAndEstado(UUID prestatarioId, EstadoPrestamo estado);

    boolean existsByPrestatarioIdAndEstadoAndFechaVencimientoBefore(
            UUID prestatarioId, EstadoPrestamo estado, LocalDate fecha);
}
