package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alumno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumnoEntidad {

    @Id
    @Column(name = "id_alumno")
    private Long idAlumno;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_alumno")
    private PersonaEntidad persona;

    @Column(name = "cui", nullable = false, unique = true, length = 8)
    private String cui;
}