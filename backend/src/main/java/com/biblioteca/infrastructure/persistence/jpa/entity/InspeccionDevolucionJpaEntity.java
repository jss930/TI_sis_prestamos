package com.biblioteca.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "inspecciones_devolucion")
public class InspeccionDevolucionJpaEntity {
    @Id @Column(name = "prestamo_id") public UUID prestamoId;
    @Column(name = "recibido_por_id", nullable = false) public UUID recibidoPorId;
    @Enumerated(EnumType.STRING) @Column(name = "estado_fisico", nullable = false, length = 20) public com.biblioteca.inventory.domain.model.EstadoFisico estadoFisico;
    @Column(name = "accesorios_faltantes", nullable = false) public boolean accesoriosFaltantes;
    @Column(nullable = false, length = 1000) public String observaciones;
    @Column(nullable = false) public java.time.Instant fecha;
}
