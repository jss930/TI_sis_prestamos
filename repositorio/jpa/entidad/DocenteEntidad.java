package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "docente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteEntidad {

    @Id
    @Column(name = "id_docente")
    private Long idDocente;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_docente")
    private PersonaEntidad persona;

    @Column(name = "departamento", nullable = false, length = 30)
    private String departamento;

    @Column(name = "categoria", nullable = false, length = 30)
    private String categoria;
}