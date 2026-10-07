package com.universidad.biblioteca.infraestructura.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilEntidad {

    @Id
    @Column(name = "id_perfil")
    private Long idPerfil;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_perfil")
    private PersonaEntidad persona;

    @Column(name = "prestamo_domicilio_equipos", nullable = false)
    private Boolean prestamoDomicilioEquipos = true;

    @Column(name = "plazo_extendido_libros", nullable = false)
    private Boolean plazoExtendidoLibros = true;

    @Column(name = "cantidad_maxima_simultanea", nullable = false)
    private Short cantidadMaximaSimultanea = 2;
}