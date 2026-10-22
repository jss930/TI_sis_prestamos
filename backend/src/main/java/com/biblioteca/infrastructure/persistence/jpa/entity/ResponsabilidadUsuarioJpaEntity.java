package com.biblioteca.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "responsabilidades_usuarios")
public class ResponsabilidadUsuarioJpaEntity {
    @Id @Column(name = "usuario_id") public UUID usuarioId;
    @Column(name = "capacitacion_verificada", nullable = false) public boolean capacitacionVerificada;
    @Column(nullable = false, length = 200) public String categorias;
}
