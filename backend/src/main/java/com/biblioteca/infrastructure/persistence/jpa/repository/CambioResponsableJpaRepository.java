package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.CambioResponsableJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CambioResponsableJpaRepository extends JpaRepository<CambioResponsableJpaEntity, UUID> {
    java.util.List<CambioResponsableJpaEntity> findAllByOrderByFechaDesc();
}
