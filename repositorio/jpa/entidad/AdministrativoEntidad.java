package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "administrativo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministrativoEntidad {

    @Id
    @Column(name = "id_administrativo")
    private Long idAdministrativo;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_administrativo")
    private PersonaEntidad persona;

    @Column(name = "area", nullable = false, length = 30)
    private String area;
}