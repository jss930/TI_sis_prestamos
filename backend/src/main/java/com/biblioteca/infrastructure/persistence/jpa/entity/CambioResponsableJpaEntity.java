package com.biblioteca.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "historial_responsables")
public class CambioResponsableJpaEntity {
    @Id public UUID id;
    @Column(name = "usuario_id", nullable = false) public UUID usuarioId;
    @Column(name = "operador_id", nullable = false) public UUID operadorId;
    @Enumerated(EnumType.STRING) @Column(length = 30) public com.biblioteca.identity.domain.model.CategoriaResponsable categoria;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) public com.biblioteca.identity.domain.model.CambioResponsable.Accion accion;
    @Column(nullable = false) public java.time.Instant fecha;
    @Enumerated(EnumType.STRING) @Column(length = 20) public com.biblioteca.identity.domain.model.CargoResponsable cargo;
}
