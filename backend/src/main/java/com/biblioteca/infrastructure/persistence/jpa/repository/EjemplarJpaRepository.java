package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.EjemplarJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EjemplarJpaRepository extends JpaRepository<EjemplarJpaEntity, UUID> {

    Optional<EjemplarJpaEntity> findByBarcode(String barcode);

    List<EjemplarJpaEntity> findByCatalogoItemId(UUID catalogoItemId);

    boolean existsByBarcode(String barcode);
}
