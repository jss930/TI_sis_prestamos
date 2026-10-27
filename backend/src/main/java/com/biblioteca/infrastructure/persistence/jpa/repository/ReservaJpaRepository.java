package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.infrastructure.persistence.jpa.entity.ReservaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaJpaRepository extends JpaRepository<ReservaJpaEntity, UUID> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from ReservaJpaEntity e where e.id = :id")
    Optional<ReservaJpaEntity> bloquearPorId(@org.springframework.data.repository.query.Param("id") UUID id);

    List<ReservaJpaEntity> findByPrestatarioId(UUID prestatarioId);

    List<ReservaJpaEntity> findByEstado(EstadoReserva estado);

    long countByPrestatarioIdAndEstado(UUID prestatarioId, EstadoReserva estado);
}
