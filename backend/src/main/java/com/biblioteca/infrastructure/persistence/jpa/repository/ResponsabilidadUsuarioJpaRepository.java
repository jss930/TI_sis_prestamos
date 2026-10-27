package com.biblioteca.infrastructure.persistence.jpa.repository;

import com.biblioteca.infrastructure.persistence.jpa.entity.ResponsabilidadUsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ResponsabilidadUsuarioJpaRepository extends JpaRepository<ResponsabilidadUsuarioJpaEntity, UUID> {

}
