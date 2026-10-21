package com.universidad.biblioteca.infraestructura.persistencia.entidades;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "persona", indexes = {
    @Index(name = "idx_persona_contacto_correo", columnList = "contacto_correo", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long idPersona;

    @Column(name = "nombres", nullable = false, length = 75)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 75)
    private String apellidos;

    @Column(name = "contacto_correo", nullable = false, length = 255, unique = true)
    private String contactoCorreo;

    @Column(name = "contacto_telefono", length = 20)
    private String contactoTelefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPersona estado = EstadoPersona.ACTIVA;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AdminEntidad admin;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PerfilEntidad perfil;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AlumnoEntidad alumno;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private DocenteEntidad docente;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AdministrativoEntidad administrativo;

    @OneToMany(mappedBy = "persona", fetch = FetchType.LAZY)
    private List<PrestamoEntidad> prestamos;

    @OneToMany(mappedBy = "persona", fetch = FetchType.LAZY)
    private List<ReservaEntidad> reservas;

    @OneToMany(mappedBy = "persona", fetch = FetchType.LAZY)
    private List<SancionEntidad> sanciones;

    @OneToMany(mappedBy = "persona", fetch = FetchType.LAZY)
    private List<HistorialPrestamoEntidad> historial;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum EstadoPersona {
        ACTIVA, SUSPENDIDA, INHABILITADA
    }
}