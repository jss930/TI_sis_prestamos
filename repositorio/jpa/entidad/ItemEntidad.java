package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item", indexes = {
    @Index(name = "idx_item_categoria", columnList = "categoria")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Inheritance(strategy = InheritanceType.JOINED)
public class ItemEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 10)
    private CategoriaItem categoria;

    @Column(name = "cantidad_total", nullable = false)
    private Integer cantidadTotal = 0;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible = 0;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private LibroEntidad libro;

    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private EquipoEntidad equipo;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private java.util.List<EjemplarEntidad> ejemplares;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private java.util.List<ReservaEntidad> reservas;

    public enum CategoriaItem {
        LIBRO, EQUIPO, OTRO
    }
}