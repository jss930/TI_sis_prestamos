package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.infrastructure.persistence.jpa.entity.ReservaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservaJpaRepository extends JpaRepository<ReservaJpaEntity, UUID> {

    List<ReservaJpaEntity> findByPrestatarioId(UUID prestatarioId);

    List<ReservaJpaEntity> findByEstado(EstadoReserva estado);

    long countByPrestatarioIdAndEstado(UUID prestatarioId, EstadoReserva estado);
}
