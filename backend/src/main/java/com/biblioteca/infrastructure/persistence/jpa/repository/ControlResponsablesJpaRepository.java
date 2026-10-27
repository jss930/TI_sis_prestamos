package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.ControlResponsablesJpaEntity;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;

public interface ControlResponsablesJpaRepository extends JpaRepository<ControlResponsablesJpaEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ControlResponsablesJpaEntity c where c.id = 1")
    ControlResponsablesJpaEntity bloquear();
}
