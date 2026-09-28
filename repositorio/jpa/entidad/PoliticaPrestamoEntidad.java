package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "politica_prestamo", uniqueConstraints = {
    @UniqueConstraint(name = "uq_politica_persona_item", columnNames = {"tipo_persona", "tipo_item"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoliticaPrestamoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_politica")
    private Long idPolitica;

    @Column(name = "tipo_persona", nullable = false, length = 30)
    private String tipoPersona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false, length = 10)
    private ItemEntidad.CategoriaItem tipoItem;

    @Column(name = "duracion_maxima_dias", nullable = false)
    private Integer duracionMaximaDias;

    @Column(name = "cantidad_maxima_simultanea", nullable = false)
    private Integer cantidadMaximaSimultanea;

    @Column(name = "permite_prestamo_domicilio", nullable = false)
    private Boolean permitePrestamoDomicilio = true;
}