package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.CatalogoItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CatalogoItemJpaRepository extends JpaRepository<CatalogoItemJpaEntity, UUID> {

    boolean existsByIsbn(String isbn);
}
