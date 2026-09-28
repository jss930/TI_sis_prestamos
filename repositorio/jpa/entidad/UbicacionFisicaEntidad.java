package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ubicacion_fisica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionFisicaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Long idUbicacion;

    @Column(name = "sala", nullable = false, length = 50)
    private String sala;

    @Column(name = "estante", nullable = false, length = 50)
    private String estante;

    @OneToMany(mappedBy = "ubicacion", fetch = FetchType.LAZY)
    private java.util.List<EjemplarEntidad> ejemplares;
}