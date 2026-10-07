package com.universidad.biblioteca.infraestructura.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "prestamo", indexes = {
    @Index(name = "idx_prestamo_persona", columnList = "id_persona"),
    @Index(name = "idx_prestamo_ejemplar", columnList = "id_ejemplar"),
    @Index(name = "idx_prestamo_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long idPrestamo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private PersonaEntidad persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ejemplar", nullable = false)
    private EjemplarEntidad ejemplar;

    @Column(name = "fecha_inicio", nullable = false)
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_vencimiento", nullable = false)
    private OffsetDateTime fechaVencimiento;

    @Column(name = "fecha_devolucion_real")
    private OffsetDateTime fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "lugar_uso", nullable = false, length = 15)
    private LugarUso lugarUso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    @OneToMany(mappedBy = "prestamo", fetch = FetchType.LAZY)
    private java.util.List<SancionEntidad> sanciones;

    @OneToMany(mappedBy = "prestamo", fetch = FetchType.LAZY)
    private java.util.List<HistorialPrestamoEntidad> historial;

    public enum EstadoPrestamo {
        ACTIVO, DEVUELTO, VENCIDO, PERDIDO
    }

    public enum LugarUso {
        EN_CAMPUS, DOMICILIO
    }
}