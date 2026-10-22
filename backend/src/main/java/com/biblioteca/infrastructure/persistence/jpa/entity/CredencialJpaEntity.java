package com.biblioteca.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "credenciales")
public class CredencialJpaEntity {

    @Id
    @Column(name = "usuario_id")
    public UUID usuarioId;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    public CredencialJpaEntity() {
    }
}
