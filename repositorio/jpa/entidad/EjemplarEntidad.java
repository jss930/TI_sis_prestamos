package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ejemplar", indexes = {
    @Index(name = "idx_ejemplar_codigo_inventario", columnList = "codigo_inventario", unique = true),
    @Index(name = "idx_ejemplar_item_disponible", columnList = "id_item, disponible")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EjemplarEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejemplar")
    private Long idEjemplar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", nullable = false)
    private ItemEntidad item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion")
    private UbicacionFisicaEntidad ubicacion;

    @Column(name = "codigo_inventario", nullable = false, unique = true, length = 50)
    private String codigoInventario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_fisico", nullable = false, length = 15)
    private EstadoFisico estadoFisico = EstadoFisico.DISPONIBLE;

    @Column(name = "disponible", nullable = false)
    private Boolean disponible = true;

    @OneToMany(mappedBy = "ejemplar", fetch = FetchType.LAZY)
    private java.util.List<PrestamoEntidad> prestamos;

    @OneToMany(mappedBy = "ejemplar", fetch = FetchType.LAZY)
    private java.util.List<ReservaEntidad> reservas;

    public enum EstadoFisico {
        DISPONIBLE, DANADO, PERDIDO
    }
}