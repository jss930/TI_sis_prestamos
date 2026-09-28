package pe.edu.unsa.sisprestamos.repositorio.jpa.entidad;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEntidad {

    @Id
    @Column(name = "id_admin")
    private Long idAdmin;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_admin")
    private PersonaEntidad persona;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permisos", columnDefinition = "jsonb", nullable = false)
    private List<String> permisos;
}