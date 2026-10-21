package com.biblioteca.infrastructure.persistence.jpa.entity;

import com.biblioteca.penalties.domain.model.EstadoSancion;
import com.biblioteca.penalties.domain.model.MotivoSancion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Entidad tecnica del contexto penalties. */
@Entity
@Table(name = "sanciones")
public class SancionJpaEntity {

    @Id
    public UUID id;

    @Column(name = "usuario_id", nullable = false)
    public UUID usuarioId;

    @Column(name = "prestamo_id")
    public UUID prestamoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public MotivoSancion motivo;

    @Column(name = "dias_suspension", nullable = false)
    public int diasSuspension;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal multa;

    @Column(nullable = false, length = 3)
    public String moneda;

    @Column(name = "fecha_inicio", nullable = false)
    public LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    public LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EstadoSancion estado;

    @Column(length = 500)
    public String observaciones;

    public SancionJpaEntity() {
    }
}
