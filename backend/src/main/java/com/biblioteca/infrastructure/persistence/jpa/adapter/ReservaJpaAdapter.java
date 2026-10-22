package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.jpa.entity.ReservaJpaEntity;
import com.biblioteca.infrastructure.persistence.jpa.repository.ReservaJpaRepository;
import com.biblioteca.reservations.domain.model.EstadoReserva;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "jpa", matchIfMissing = true)
public class ReservaJpaAdapter implements ReservaRepository {

    private final ReservaJpaRepository repositorio;

    public ReservaJpaAdapter(ReservaJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Reserva guardar(Reserva reserva) {
        repositorio.save(aEntidad(reserva));
        return reserva;
    }

    @Override
    public Optional<Reserva> buscarPorId(ReservaId id) {
        return repositorio.findById(id.value()).map(ReservaJpaAdapter::aDominio);
    }

    @Override
    public List<Reserva> listarTodos() {
        return repositorio.findAll().stream().map(ReservaJpaAdapter::aDominio).toList();
    }

    @Override
    public List<Reserva> listarPorPrestatario(UsuarioId prestatarioId) {
        return repositorio.findByPrestatarioId(prestatarioId.value()).stream()
                .map(ReservaJpaAdapter::aDominio).toList();
    }

    @Override
    public List<Reserva> listarPorPrestatarioYEstado(UsuarioId prestatarioId, EstadoReserva estado) {
        return repositorio.findByPrestatarioId(prestatarioId.value()).stream()
                .map(ReservaJpaAdapter::aDominio)
                .filter(r -> r.estado() == estado)
                .toList();
    }

    @Override
    public List<Reserva> listarPorEstado(EstadoReserva estado) {
        return repositorio.findByEstado(estado).stream()
                .map(ReservaJpaAdapter::aDominio).toList();
    }

    @Override
    public long contarActivasPor(UsuarioId prestatarioId) {
        return repositorio.countByPrestatarioIdAndEstado(
                prestatarioId.value(), EstadoReserva.SOLICITADA);
    }

    private static ReservaJpaEntity aEntidad(Reserva reserva) {
        ReservaJpaEntity entidad = new ReservaJpaEntity();
        entidad.id = reserva.id().value();
        entidad.prestatarioId = reserva.prestatarioId().value();
        entidad.catalogoItemId = reserva.catalogoItemId().value();
        entidad.operadorId = reserva.operadorId() == null ? null : reserva.operadorId().value();
        entidad.prestamoId = reserva.prestamoId() == null ? null : reserva.prestamoId().value();
        entidad.estado = reserva.estado();
        entidad.motivo = reserva.motivo();
        entidad.fechaAlta = reserva.fechaAlta();
        entidad.fechaDecision = reserva.fechaDecision();
        return entidad;
    }

    private static Reserva aDominio(ReservaJpaEntity entidad) {
        return Reserva.reconstruir(
                new ReservaId(entidad.id),
                new UsuarioId(entidad.prestatarioId),
                new CatalogoItemId(entidad.catalogoItemId),
                entidad.estado,
                entidad.operadorId == null ? null : new UsuarioId(entidad.operadorId),
                entidad.prestamoId == null ? null : new PrestamoId(entidad.prestamoId),
                entidad.motivo,
                entidad.fechaAlta,
                entidad.fechaDecision);
    }
}
