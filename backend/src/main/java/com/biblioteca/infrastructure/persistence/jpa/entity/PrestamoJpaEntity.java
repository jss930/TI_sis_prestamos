package com.biblioteca.infrastructure.persistence.jpa.entity;

import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.circulation.domain.model.LugarUso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Entidad tecnica del contexto circulation. */
@Entity
@Table(name = "prestamos")
public class PrestamoJpaEntity {

    @Id
    public UUID id;

    @Column(name = "operador_id", nullable = false)
    public UUID operadorId;

    @Column(name = "prestatario_id", nullable = false)
    public UUID prestatarioId;

    @Column(name = "ejemplar_id", nullable = false)
    public UUID ejemplarId;

    @Column(name = "fecha_inicio", nullable = false)
    public LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento", nullable = false)
    public LocalDate fechaVencimiento;

    @Column(name = "fecha_devolucion")
    public LocalDate fechaDevolucion;

    @Enumerated(EnumType.STRING)
    @Column(name = "lugar_uso", nullable = false, length = 20)
    public LugarUso lugarUso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EstadoPrestamo estado;

    @Column(nullable = false)
    public int renovaciones;

    @Column(length = 500)
    public String observaciones;

    @Column(name = "fecha_alta", nullable = false)
    public Instant fechaAlta;

    @Column(name = "fecha_hora_entrega", nullable = false)
    public Instant fechaHoraEntrega;

    @Column(name = "fecha_hora_vencimiento", nullable = false)
    public Instant fechaHoraVencimiento;

    @Column(name = "fecha_hora_devolucion")
    public Instant fechaHoraDevolucion;

    public PrestamoJpaEntity() {
    }
}
