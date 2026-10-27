package com.biblioteca.infrastructure.persistence.jpa.entity;

import com.biblioteca.identity.domain.model.EstadoCuentaUsuario;
import com.biblioteca.identity.domain.model.PerfilPrestatario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Entidad tecnica del contexto identity. */
@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    public UUID id;

    @Column(nullable = false, unique = true, length = 255)
    public String email;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    public String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public PerfilPrestatario perfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EstadoCuentaUsuario estado;

    /**
     * Lista separada por comas. Se eligio un campo simple para no acoplar
     * el agregado a una coleccion JPA y mantener el adaptador explícito.
     */
    @Column(nullable = false, length = 500)
    public String permisos;

    @Column(name = "fecha_alta", nullable = false)
    public Instant fechaAlta;

    @Column(name = "fecha_validacion")
    public Instant fechaValidacion;

    @Column(name = "motivo_suspension", length = 500)
    public String motivoSuspension;

    @Column(name = "codigo_institucional", length = 30)
    public String codigoInstitucional;

    @Column(name = "validado_por_id")
    public UUID validadoPorId;

    @Column(name = "observaciones_revision", length = 1000)
    public String observacionesRevision;

    @Column(name = "revisado_por_id")
    public UUID revisadoPorId;

    @Column(name = "fecha_revision")
    public Instant fechaRevision;

    @Column(name = "correccion_pendiente", nullable = false)
    public boolean correccionPendiente;

    public UsuarioJpaEntity() {
    }
}
