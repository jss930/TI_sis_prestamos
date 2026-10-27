package com.biblioteca.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "control_responsables")
public class ControlResponsablesJpaEntity {
    @Id public Integer id;
}
