package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "libro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroEntidad {

    @Id
    @Column(name = "id_item")
    private Long idItem;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_item")
    private ItemEntidad item;

    @Column(name = "isbn", nullable = false, unique = true, length = 18)
    private String isbn;

    @Column(name = "autor", nullable = false, length = 100)
    private String autor;

    @Column(name = "editorial", nullable = false, length = 100)
    private String editorial;

    @Column(name = "edicion", length = 30)
    private String edicion;
}