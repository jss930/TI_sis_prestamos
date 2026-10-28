package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.infrastructure.persistence.jpa.entity.PrestamoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PrestamoJpaRepository extends JpaRepository<PrestamoJpaEntity, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from PrestamoJpaEntity e where e.id = :id")
    java.util.Optional<PrestamoJpaEntity> buscarParaActualizar(@org.springframework.data.repository.query.Param("id") UUID id);


    List<PrestamoJpaEntity> findByPrestatarioId(UUID prestatarioId);

    boolean existsByEjemplarIdAndEstado(UUID ejemplarId, EstadoPrestamo estado);

    long countByPrestatarioIdAndEstado(UUID prestatarioId, EstadoPrestamo estado);

    boolean existsByPrestatarioIdAndEstadoAndFechaVencimientoBefore(
            UUID prestatarioId, EstadoPrestamo estado, LocalDate fecha);

    @org.springframework.data.jpa.repository.Query("""
            select count(p) from PrestamoJpaEntity p
            where p.ejemplarId = :ejemplarId
              and p.estado = com.biblioteca.circulation.domain.model.EstadoPrestamo.ACTIVO
              and p.fechaHoraEntrega < :fin
              and p.fechaHoraVencimiento > :inicio
            """)
    long contarSolapamientos(
            @org.springframework.data.repository.query.Param("ejemplarId") UUID ejemplarId,
            @org.springframework.data.repository.query.Param("inicio") Instant inicio,
            @org.springframework.data.repository.query.Param("fin") Instant fin);
}
