package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.InspeccionDevolucionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InspeccionDevolucionJpaRepository extends JpaRepository<InspeccionDevolucionJpaEntity, UUID> {
    java.util.List<InspeccionDevolucionJpaEntity> findAllByOrderByFechaDesc();
}
