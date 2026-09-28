package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "historial_prestamo", indexes = {
    @Index(name = "idx_historial_persona", columnList = "id_persona"),
    @Index(name = "idx_historial_prestamo", columnList = "id_prestamo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialPrestamoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private PersonaEntidad persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestamo", nullable = false)
    private PrestamoEntidad prestamo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 15)
    private TipoEventoHistorial tipoEvento;

    @Column(name = "fecha_evento", nullable = false)
    private OffsetDateTime fechaEvento;

    public enum TipoEventoHistorial {
        CREADO, DEVUELTO, VENCIDO, SANCIONADO
    }
}