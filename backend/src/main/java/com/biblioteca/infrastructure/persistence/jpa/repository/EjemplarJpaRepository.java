package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.EjemplarJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EjemplarJpaRepository extends JpaRepository<EjemplarJpaEntity, UUID> {

    Optional<EjemplarJpaEntity> findByBarcode(String barcode);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from EjemplarJpaEntity e where e.id = :id")
    Optional<EjemplarJpaEntity> bloquearPorId(@org.springframework.data.repository.query.Param("id") UUID id);

    List<EjemplarJpaEntity> findByCatalogoItemId(UUID catalogoItemId);

    boolean existsByBarcode(String barcode);
}
