package com.biblioteca.infrastructure.persistence.jpa.entity;

import com.biblioteca.catalog.domain.model.TipoItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad tecnica de persistencia. No es el agregado de dominio: el adaptador
 * se encarga de reconstruirlo y de mantener los invariantes en el dominio.
 */
@Entity
@Table(name = "catalogo_items")
public class CatalogoItemJpaEntity {

    @Id
    public UUID id;

    @Column(nullable = false, length = 150)
    public String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public TipoItem tipo;

    @Column(nullable = false)
    public boolean activo;

    @Column(name = "fecha_alta", nullable = false)
    public Instant fechaAlta;

    @Column(length = 20)
    public String isbn;

    @Column(length = 120)
    public String autor;

    @Column(length = 150)
    public String editorial;

    @Column(name = "anio_publicacion")
    public Integer anioPublicacion;

    @Column(length = 120)
    public String marca;

    @Column(length = 120)
    public String modelo;

    @Column(length = 500)
    public String descripcion;

    public CatalogoItemJpaEntity() {
    }
}
