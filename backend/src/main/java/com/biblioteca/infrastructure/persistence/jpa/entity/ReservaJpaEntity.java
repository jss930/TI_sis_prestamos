package com.biblioteca.infrastructure.persistence.jpa.entity;

import com.biblioteca.circulation.domain.model.LugarUso;

import com.biblioteca.reservations.domain.model.EstadoReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Entidad tecnica del contexto reservations. */
@Entity
@Table(name = "reservas")
public class ReservaJpaEntity {

    @Id
    public UUID id;

    @Column(name = "prestatario_id", nullable = false)
    public UUID prestatarioId;

    @Column(name = "catalogo_item_id", nullable = false)
    public UUID catalogoItemId;

    @Column(name = "operador_id")
    public UUID operadorId;

    @Column(name = "prestamo_id")
    public UUID prestamoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EstadoReserva estado;

    @Column(length = 500)
    public String motivo;

    @Column(name = "fecha_alta", nullable = false)
    public Instant fechaAlta;

    @Column(name = "fecha_decision")
    public Instant fechaDecision;

    @Column(name = "ejemplar_id")
    public UUID ejemplarId;

    @Column(name = "fecha_limite_retiro")
    public Instant fechaLimiteRetiro;

    @Column(name = "fecha_entrega")
    public Instant fechaEntrega;

    @Column(name = "entregado_por_id")
    public UUID entregadoPorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "lugar_uso", nullable = false, length = 20)
    public LugarUso lugarUso;

    public ReservaJpaEntity() {
    }
}
