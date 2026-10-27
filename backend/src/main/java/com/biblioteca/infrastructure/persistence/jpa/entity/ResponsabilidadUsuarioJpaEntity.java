package com.biblioteca.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "responsabilidades_usuarios")
public class ResponsabilidadUsuarioJpaEntity {
    @Id @Column(name = "usuario_id") public UUID usuarioId;
    @Column(name = "capacitacion_verificada", nullable = false) public boolean capacitacionVerificada;
    @Column(nullable = false, length = 200) public String categorias;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cargos_responsables", joinColumns = @JoinColumn(name = "usuario_id"))
    @MapKeyColumn(name = "categoria", length = 30)
    @MapKeyEnumerated(EnumType.STRING)
    @Enumerated(EnumType.STRING)
    @Column(name = "cargo", nullable = false, length = 20)
    public java.util.Map<com.biblioteca.identity.domain.model.CategoriaResponsable,
            com.biblioteca.identity.domain.model.CargoResponsable> cargos = new java.util.EnumMap<>(com.biblioteca.identity.domain.model.CategoriaResponsable.class);
}
