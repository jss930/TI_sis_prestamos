package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.jpa.entity.SancionJpaEntity;
import com.biblioteca.infrastructure.persistence.jpa.repository.SancionJpaRepository;
import com.biblioteca.penalties.domain.model.EstadoSancion;
import com.biblioteca.penalties.domain.model.Sancion;
import com.biblioteca.penalties.domain.repository.SancionRepository;
import com.biblioteca.penalties.domain.vo.MontoMulta;
import com.biblioteca.penalties.domain.vo.SancionId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto de sanciones. */
@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "jpa", matchIfMissing = true)
public class SancionJpaAdapter implements SancionRepository {

    private final SancionJpaRepository repositorio;

    public SancionJpaAdapter(SancionJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Sancion guardar(Sancion sancion) {
        repositorio.save(aEntidad(sancion));
        return sancion;
    }

    @Override
    public Optional<Sancion> buscarPorId(SancionId id) {
        return repositorio.findById(id.value()).map(SancionJpaAdapter::aDominio);
    }

    @Override
    public List<Sancion> listarTodas() {
        return repositorio.findAll().stream().map(SancionJpaAdapter::aDominio).toList();
    }

    @Override
    public List<Sancion> listarPorUsuario(UsuarioId usuarioId) {
        return repositorio.findByUsuarioId(usuarioId.value()).stream()
                .map(SancionJpaAdapter::aDominio).toList();
    }

    @Override
    public boolean existeActivaPara(UsuarioId usuarioId, LocalDate fecha) {
        return repositorio.existsByUsuarioIdAndEstadoAndFechaInicioLessThanEqualAndFechaFinGreaterThan(
                usuarioId.value(), EstadoSancion.ACTIVA, fecha, fecha);
    }

    @Override
    public boolean existePorPrestamo(PrestamoId prestamoId) {
        return prestamoId != null && repositorio.existsByPrestamoId(prestamoId.value());
    }

    private static SancionJpaEntity aEntidad(Sancion sancion) {
        SancionJpaEntity entidad = new SancionJpaEntity();
        entidad.id = sancion.id().value();
        entidad.usuarioId = sancion.usuarioId().value();
        entidad.prestamoId = sancion.prestamoId() == null ? null : sancion.prestamoId().value();
        entidad.motivo = sancion.motivo();
        entidad.diasSuspension = sancion.diasSuspension();
        entidad.multa = sancion.multa().monto();
        entidad.moneda = sancion.multa().moneda();
        entidad.fechaInicio = sancion.fechaInicio();
        entidad.fechaFin = sancion.fechaFin();
        entidad.estado = sancion.estado();
        entidad.observaciones = sancion.observaciones();
        return entidad;
    }

    private static Sancion aDominio(SancionJpaEntity entidad) {
        return Sancion.reconstruir(new SancionId(entidad.id),
                new UsuarioId(entidad.usuarioId),
                entidad.prestamoId == null ? null : new PrestamoId(entidad.prestamoId),
                entidad.motivo, entidad.diasSuspension,
                new MontoMulta(entidad.multa, entidad.moneda),
                entidad.fechaInicio, entidad.fechaFin, entidad.estado, entidad.observaciones);
    }
}
