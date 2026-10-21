package com.universidad.biblioteca.infraestructura.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sancion", indexes = {
    @Index(name = "idx_sancion_persona", columnList = "id_persona"),
    @Index(name = "idx_sancion_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SancionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sancion")
    private Long idSancion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private PersonaEntidad persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestamo", nullable = false)
    private PrestamoEntidad prestamo;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 20)
    private MotivoSancion motivo;

    @Column(name = "fecha_inicio", nullable = false)
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private OffsetDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoSancion estado = EstadoSancion.ACTIVA;

    public enum MotivoSancion {
        DEVOLUCION_TARDIA, DANO, PERDIDA
    }

    public enum EstadoSancion {
        ACTIVA, CUMPLIDA
    }
}