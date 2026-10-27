package com.biblioteca.identity.domain.model;

import com.biblioteca.identity.domain.event.PermisoOtorgadoEvent;
import com.biblioteca.identity.domain.event.UsuarioProvisionalRegistradoEvent;
import com.biblioteca.identity.domain.event.UsuarioReactivadoEvent;
import com.biblioteca.identity.domain.event.UsuarioSuspendidoEvent;
import com.biblioteca.identity.domain.event.UsuarioValidadoEvent;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.CodigoInstitucional;
import com.biblioteca.identity.domain.vo.NombreCompleto;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.shared.domain.AggregateRoot;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Cuenta unificada de una persona en la biblioteca.
 *
 * <p>Regla central: una cuenta PROVISIONAL puede consultar el catalogo, pero
 * no solicitar prestamos ni operar en ventanilla hasta ser validada.</p>
 */
public final class Usuario extends AggregateRoot<UsuarioId> {

    private final UsuarioId id;
    private EmailAddress email;
    private NombreCompleto nombreCompleto;
    private PerfilPrestatario perfil;
    private final Instant fechaAlta;
    private final Set<PermisoOperativo> permisos;

    private EstadoCuentaUsuario estado;
    private Instant fechaValidacion;
    private String motivoSuspension;
    private CodigoInstitucional codigoInstitucional;
    private UsuarioId validadoPorId;
    private String observacionesRevision;
    private UsuarioId revisadoPorId;
    private Instant fechaRevision;
    private boolean correccionPendiente;


    private Usuario(UsuarioId id, EmailAddress email, NombreCompleto nombreCompleto,
                    PerfilPrestatario perfil, EstadoCuentaUsuario estado,
                    Set<PermisoOperativo> permisos, Instant fechaAlta,
                    Instant fechaValidacion, String motivoSuspension) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.nombreCompleto = Objects.requireNonNull(nombreCompleto);
        this.perfil = Objects.requireNonNull(perfil);
        this.estado = Objects.requireNonNull(estado);
        this.permisos = permisos.isEmpty()
                ? EnumSet.noneOf(PermisoOperativo.class)
                : EnumSet.copyOf(permisos);
        this.fechaAlta = Objects.requireNonNull(fechaAlta);
        this.fechaValidacion = fechaValidacion;
        this.motivoSuspension = normalizarMotivo(motivoSuspension);
        validarCoherencia();
    }

    public static Usuario registrarProvisional(EmailAddress email, NombreCompleto nombreCompleto,
                                               PerfilPrestatario perfil) {
        Usuario usuario = new Usuario(UsuarioId.nuevo(), email, nombreCompleto, perfil,
                EstadoCuentaUsuario.PROVISIONAL, EnumSet.noneOf(PermisoOperativo.class),
                Instant.now(), null, null);
        usuario.registrarEvento(new UsuarioProvisionalRegistradoEvent(
                usuario.id, email, perfil, usuario.fechaAlta));
        return usuario;
    }

    public static Usuario reconstruir(UsuarioId id, EmailAddress email, NombreCompleto nombreCompleto,
                                      PerfilPrestatario perfil, EstadoCuentaUsuario estado,
                                      Set<PermisoOperativo> permisos, Instant fechaAlta,
                                      Instant fechaValidacion, String motivoSuspension) {
        return new Usuario(id, email, nombreCompleto, perfil, estado, permisos,
                fechaAlta, fechaValidacion, motivoSuspension);
    }

    public static Usuario reconstruirConRevision(UsuarioId id, EmailAddress email, NombreCompleto nombre,
            PerfilPrestatario perfil, EstadoCuentaUsuario estado, Set<PermisoOperativo> permisos, Instant fechaAlta,
            Instant fechaValidacion, String motivoSuspension, CodigoInstitucional codigo, UsuarioId validadoPorId,
            String observaciones, UsuarioId revisadoPorId, Instant fechaRevision, boolean correccionPendiente) {
        var usuario = reconstruir(id, email, nombre, perfil, estado, permisos, fechaAlta, fechaValidacion, motivoSuspension);
        usuario.codigoInstitucional = codigo; usuario.validadoPorId = validadoPorId;
        usuario.observacionesRevision = observaciones; usuario.revisadoPorId = revisadoPorId;
        usuario.fechaRevision = fechaRevision; usuario.correccionPendiente = correccionPendiente;
        return usuario;
    }

    /** Compatibilidad para reconstrucciones y fixtures anteriores a la revision administrativa. */
    public void validar() { completarValidacion(null, Instant.now()); }

    public void validar(UsuarioId operadorId, Instant ahora, boolean datosRevisados) {
        exigirEstado(EstadoCuentaUsuario.PROVISIONAL, "VALIDAR");
        exigirRevisorAjeno(operadorId);
        if (!datosRevisados) throw new BusinessRuleException("REVISION_REQUERIDA", "Confirma que revisaste los datos de la cuenta");
        if (codigoInstitucional == null) throw new BusinessRuleException("CODIGO_INSTITUCIONAL_REQUERIDO", "Completa el codigo institucional antes de validar");
        if (correccionPendiente) throw new BusinessRuleException("CORRECCION_PENDIENTE", "El usuario debe enviar sus datos corregidos antes de la validacion");
        completarValidacion(operadorId, ahora);
    }

    private void completarValidacion(UsuarioId operadorId, Instant ahora) {
        exigirEstado(EstadoCuentaUsuario.PROVISIONAL, "VALIDAR");
        estado = EstadoCuentaUsuario.VALIDADO;
        fechaValidacion = Objects.requireNonNull(ahora);
        validadoPorId = operadorId;
        motivoSuspension = null;
        registrarEvento(new UsuarioValidadoEvent(id, fechaValidacion));
    }

    public void actualizarDatosPendientes(EmailAddress email, NombreCompleto nombre, PerfilPrestatario perfil,
                                          CodigoInstitucional codigo) {
        exigirEstado(EstadoCuentaUsuario.PROVISIONAL, "ACTUALIZAR_DATOS");
        Objects.requireNonNull(email);
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(perfil);
        if (codigo == null) throw new DomainException("CODIGO_INSTITUCIONAL_REQUERIDO", "Indica el codigo universitario o de personal");
        this.email = email;
        this.nombreCompleto = nombre;
        this.perfil = perfil;
        this.codigoInstitucional = codigo;
        this.correccionPendiente = false;
    }

    public void solicitarCorreccion(UsuarioId operadorId, String observaciones, Instant ahora) {
        exigirEstado(EstadoCuentaUsuario.PROVISIONAL, "SOLICITAR_CORRECCION");
        exigirRevisorAjeno(operadorId);
        String texto = normalizarMotivo(observaciones);
        if (texto == null || texto.length() > 1000)
            throw new DomainException("OBSERVACIONES_REQUERIDAS", "Indica que datos debe corregir la persona (maximo 1000 caracteres)");
        this.observacionesRevision = texto; this.revisadoPorId = operadorId;
        this.fechaRevision = Objects.requireNonNull(ahora); this.correccionPendiente = true;
    }

    private void exigirRevisorAjeno(UsuarioId operadorId) {
        if (id.equals(Objects.requireNonNull(operadorId)))
            throw new BusinessRuleException("AUTOVALIDACION_NO_PERMITIDA", "No puedes revisar tu propia cuenta");
    }

    public CodigoInstitucional codigoInstitucional() { return codigoInstitucional; }
    public UsuarioId validadoPorId() { return validadoPorId; }
    public String observacionesRevision() { return observacionesRevision; }
    public UsuarioId revisadoPorId() { return revisadoPorId; }
    public Instant fechaRevision() { return fechaRevision; }
    public boolean correccionPendiente() { return correccionPendiente; }

    public void suspender(String motivo) {
        exigirEstado(EstadoCuentaUsuario.VALIDADO, "SUSPENDER");
        motivoSuspension = normalizarMotivo(motivo);
        if (motivoSuspension == null) {
            throw new DomainException("MOTIVO_REQUERIDO", "El motivo de suspension es obligatorio");
        }
        estado = EstadoCuentaUsuario.SUSPENDIDO;
        registrarEvento(new UsuarioSuspendidoEvent(id, motivoSuspension, Instant.now()));
    }

    public void reactivar() {
        exigirEstado(EstadoCuentaUsuario.SUSPENDIDO, "REACTIVAR");
        estado = EstadoCuentaUsuario.VALIDADO;
        motivoSuspension = null;
        registrarEvento(new UsuarioReactivadoEvent(id, Instant.now()));
    }

    public void otorgarPermiso(PermisoOperativo permiso) {
        Objects.requireNonNull(permiso, "El permiso es obligatorio");
        if (estado != EstadoCuentaUsuario.VALIDADO) {
            throw new BusinessRuleException("USUARIO_NO_VALIDADO",
                    "Solo se otorgan permisos operativos a cuentas validadas");
        }
        if (permisos.add(permiso)) {
            registrarEvento(new PermisoOtorgadoEvent(id, permiso, Instant.now()));
        }
    }

    public void revocarPermiso(PermisoOperativo permiso) {
        Objects.requireNonNull(permiso, "El permiso es obligatorio");
        permisos.remove(permiso);
    }

    /** Consultar el catalogo siempre esta permitido, incluso en estado PROVISIONAL. */
    public boolean puedeConsultarCatalogo() {
        return true;
    }

    public boolean puedeSolicitarPrestamo() {
        return estado == EstadoCuentaUsuario.VALIDADO;
    }

    public boolean puedeOperarVentanilla() {
        return estado == EstadoCuentaUsuario.VALIDADO
                && permisos.contains(PermisoOperativo.REGISTRAR_PRESTAMO);
    }

    public boolean puedeRegistrarDevolucion() {
        return estado == EstadoCuentaUsuario.VALIDADO
                && permisos.contains(PermisoOperativo.REGISTRAR_DEVOLUCION);
    }

    public boolean puedeAdministrarSistema() {
        return estado == EstadoCuentaUsuario.VALIDADO
                && permisos.contains(PermisoOperativo.ADMINISTRAR_SISTEMA);
    }

    public boolean puedeValidarProvisionales() {
        return estado == EstadoCuentaUsuario.VALIDADO
                && permisos.contains(PermisoOperativo.VALIDAR_USUARIO_PROVISIONAL);
    }

    public boolean puedeGestionarSanciones() {
        return estado == EstadoCuentaUsuario.VALIDADO
                && permisos.contains(PermisoOperativo.GESTIONAR_SANCIONES);
    }

    public boolean puedeGestionarCatalogo() {
        return estado == EstadoCuentaUsuario.VALIDADO
                && permisos.contains(PermisoOperativo.GESTIONAR_CATALOGO);
    }

    public boolean puedeGestionarReservas() {
        return estado == EstadoCuentaUsuario.VALIDADO
                && permisos.contains(PermisoOperativo.GESTIONAR_RESERVAS);
    }

    public boolean tienePermiso(PermisoOperativo permiso) {
        return permisos.contains(permiso);
    }

    /**
     * Rol de acceso derivado. Una cuenta solo es administrador si esta validada
     * y tiene al menos un permiso de gestion. Los permisos son la unica fuente
     * de verdad; el perfil solo define la politica de prestamo.
     */
    public RolAcceso rolAcceso() {
        if (puedeAdministrarSistema()) {
            return RolAcceso.ADMINISTRADOR;
        }
        return switch (perfil) {
            case ALUMNO -> RolAcceso.ESTUDIANTE;
            case DOCENTE -> RolAcceso.DOCENTE;
            case ADMINISTRATIVO -> RolAcceso.ADMINISTRATIVO;
        };
    }

    private void exigirEstado(EstadoCuentaUsuario esperado, String accion) {
        if (estado != esperado) {
            throw new BusinessRuleException("TRANSICION_INVALIDA",
                    "No se puede " + accion + " una cuenta en estado " + estado);
        }
    }

    private void validarCoherencia() {
        switch (estado) {
            case PROVISIONAL -> {
                if (fechaValidacion != null) {
                    throw new DomainException("USUARIO_INCONSISTENTE",
                            "Una cuenta provisional no puede tener fecha de validacion");
                }
            }
            case VALIDADO -> {
                if (fechaValidacion == null) {
                    throw new DomainException("USUARIO_INCONSISTENTE",
                            "Una cuenta validada requiere fecha de validacion");
                }
            }
            case SUSPENDIDO -> {
                if (fechaValidacion == null || motivoSuspension == null) {
                    throw new DomainException("USUARIO_INCONSISTENTE",
                            "Una cuenta suspendida requiere validacion previa y motivo");
                }
            }
        }
    }

    private static String normalizarMotivo(String motivo) {
        return motivo == null || motivo.isBlank() ? null : motivo.trim();
    }

    @Override
    public UsuarioId id() {
        return id;
    }

    public EmailAddress email() {
        return email;
    }

    public NombreCompleto nombreCompleto() {
        return nombreCompleto;
    }

    public PerfilPrestatario perfil() {
        return perfil;
    }

    public EstadoCuentaUsuario estado() {
        return estado;
    }

    public Set<PermisoOperativo> permisos() {
        return Set.copyOf(permisos);
    }

    public Instant fechaAlta() {
        return fechaAlta;
    }

    public Instant fechaValidacion() {
        return fechaValidacion;
    }

    public String motivoSuspension() {
        return motivoSuspension;
    }

    @Override
    public boolean equals(Object otro) {
        return this == otro || (otro instanceof Usuario usuario && id.equals(usuario.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
