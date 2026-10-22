package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.CredencialJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CredencialJpaRepository extends JpaRepository<CredencialJpaEntity, UUID> {
}
