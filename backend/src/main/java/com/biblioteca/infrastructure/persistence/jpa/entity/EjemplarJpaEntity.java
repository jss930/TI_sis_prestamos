package com.biblioteca.infrastructure.persistence.jpa.entity;

import com.biblioteca.inventory.domain.model.EstadoFisico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Entidad tecnica del contexto inventory. */
@Entity
@Table(name = "ejemplares")
public class EjemplarJpaEntity {

    @Id
    public UUID id;

    @Column(name = "catalogo_item_id", nullable = false)
    public UUID catalogoItemId;

    @Column(nullable = false, unique = true, length = 50)
    public String barcode;

    @Column(nullable = false, length = 50)
    public String sala;

    @Column(nullable = false, length = 50)
    public String estante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_fisico", nullable = false, length = 20)
    public EstadoFisico estadoFisico;

    @Column(length = 500)
    public String notas;

    @Column(name = "fecha_alta", nullable = false)
    public Instant fechaAlta;

    public EjemplarJpaEntity() {
    }
}
