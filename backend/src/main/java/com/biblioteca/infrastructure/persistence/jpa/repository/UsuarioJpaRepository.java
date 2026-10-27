package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from UsuarioJpaEntity e where e.id = :id")
    java.util.Optional<UsuarioJpaEntity> buscarParaActualizar(@org.springframework.data.repository.query.Param("id") UUID id);


    Optional<UsuarioJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
