package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.circulation.domain.vo.PeriodoPrestamo;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.jpa.entity.PrestamoJpaEntity;
import com.biblioteca.infrastructure.persistence.jpa.repository.PrestamoJpaRepository;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto de circulacion. */
@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "jpa", matchIfMissing = true)
public class PrestamoJpaAdapter implements PrestamoRepository {

    private final PrestamoJpaRepository repositorio;

    public PrestamoJpaAdapter(PrestamoJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Prestamo guardar(Prestamo prestamo) {
        repositorio.save(aEntidad(prestamo));
        return prestamo;
    }

    @Override
    public Optional<Prestamo> buscarPorId(PrestamoId id) {
        return repositorio.findById(id.value()).map(PrestamoJpaAdapter::aDominio);
    }

    @Override
    public Optional<Prestamo> buscarPorIdParaActualizar(PrestamoId id) {
        return repositorio.buscarParaActualizar(id.value()).map(PrestamoJpaAdapter::aDominio);
    }

    @Override
    public List<Prestamo> listarTodos() {
        return repositorio.findAll().stream().map(PrestamoJpaAdapter::aDominio).toList();
    }

    @Override
    public List<Prestamo> listarPorPrestatario(UsuarioId prestatarioId) {
        return repositorio.findByPrestatarioId(prestatarioId.value()).stream()
                .map(PrestamoJpaAdapter::aDominio).toList();
    }

    @Override
    public boolean existeActivoPara(EjemplarId ejemplarId) {
        return repositorio.existsByEjemplarIdAndEstado(
                ejemplarId.value(), com.biblioteca.circulation.domain.model.EstadoPrestamo.ACTIVO);
    }

    @Override
    public long contarActivosPor(UsuarioId prestatarioId) {
        return repositorio.countByPrestatarioIdAndEstado(
                prestatarioId.value(), com.biblioteca.circulation.domain.model.EstadoPrestamo.ACTIVO);
    }

    @Override
    public boolean tieneVencido(UsuarioId prestatarioId, LocalDate hoy) {
        return repositorio.existsByPrestatarioIdAndEstadoAndFechaVencimientoBefore(
                prestatarioId.value(),
                com.biblioteca.circulation.domain.model.EstadoPrestamo.ACTIVO,
                hoy);
    }

    @Override
    public boolean existeSolapamiento(EjemplarId ejemplarId, Instant inicio, Instant fin) {
        return repositorio.contarSolapamientos(ejemplarId.value(), inicio, fin) > 0;
    }

    private static PrestamoJpaEntity aEntidad(Prestamo prestamo) {
        PrestamoJpaEntity entidad = new PrestamoJpaEntity();
        entidad.id = prestamo.id().value();
        entidad.operadorId = prestamo.operadorId().value();
        entidad.prestatarioId = prestamo.prestatarioId().value();
        entidad.ejemplarId = prestamo.ejemplarId().value();
        entidad.fechaInicio = prestamo.periodo().inicio();
        entidad.fechaVencimiento = prestamo.periodo().vencimiento();
        entidad.fechaDevolucion = prestamo.fechaDevolucion();
        entidad.lugarUso = prestamo.lugarUso();
        entidad.estado = prestamo.estado();
        entidad.renovaciones = prestamo.renovaciones();
        entidad.observaciones = prestamo.observaciones();
        entidad.fechaAlta = prestamo.fechaAlta();
        entidad.fechaHoraEntrega = prestamo.fechaHoraEntrega();
        entidad.fechaHoraVencimiento = prestamo.fechaHoraVencimiento();
        entidad.fechaHoraDevolucion = prestamo.fechaHoraDevolucion();
        return entidad;
    }

    private static Prestamo aDominio(PrestamoJpaEntity entidad) {
        return Prestamo.reconstruir(new PrestamoId(entidad.id),
                new UsuarioId(entidad.operadorId), new UsuarioId(entidad.prestatarioId),
                new EjemplarId(entidad.ejemplarId),
                new PeriodoPrestamo(entidad.fechaInicio, entidad.fechaVencimiento),
                entidad.lugarUso, entidad.estado, entidad.fechaDevolucion,
                entidad.renovaciones, entidad.observaciones, entidad.fechaAlta,
                entidad.fechaHoraEntrega, entidad.fechaHoraVencimiento, entidad.fechaHoraDevolucion);
    }
}
