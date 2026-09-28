package pe.edu.unsa.sisprestamos.repositorio.impl;

import pe.edu.unsa.sisprestamos.dominio.circulacion.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador.CirculacionMapeador;
import pe.edu.unsa.sisprestamos.repositorio.circulacion.IReservaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional
public class ReservaRepositorioImpl implements IReservaRepositorio {

    private final ReservaJpaRepositorio reservaJpaRepositorio;
    private final CirculacionMapeador mapeador;

    @Override
    public Reserva guardar(Reserva reserva) {
        ReservaEntidad entidad = mapeador.aEntidad(reserva);
        ReservaEntidad guardada = reservaJpaRepositorio.save(entidad);
        return mapeador.aDominio(guardada);
    }

    @Override
    public Optional<Reserva> buscarPorId(Long id) {
        return reservaJpaRepositorio.findById(id)
                .map(mapeador::aDominio);
    }

    @Override
    public List<Reserva> buscarPorPersona(Long idPersona) {
        return reservaJpaRepositorio.findByPersonaIdPersona(idPersona)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reserva> buscarPorItem(Long idItem) {
        return reservaJpaRepositorio.findByItemIdItem(idItem)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reserva> buscarPorEstado(Reserva.Estado estado) {
        ReservaEntidad.EstadoReserva estadoEntidad = ReservaEntidad.EstadoReserva.valueOf(estado.name());
        return reservaJpaRepositorio.findByEstado(estadoEntidad)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reserva> buscarPendientesPorItem(Long idItem) {
        return reservaJpaRepositorio.findPendientesByItem(idItem)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reserva> buscarExpiradas(OffsetDateTime ahora) {
        return reservaJpaRepositorio.findExpiradas(ahora)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reserva> buscarActivaPorEjemplar(Long idEjemplar) {
        return reservaJpaRepositorio.findActivaByEjemplar(idEjemplar)
                .map(mapeador::aDominio);
    }

    @Override
    public long contarPorPersonaYEstado(Long idPersona, Reserva.Estado estado) {
        ReservaEntidad.EstadoReserva estadoEntidad = ReservaEntidad.EstadoReserva.valueOf(estado.name());
        return reservaJpaRepositorio.countByPersonaIdPersonaAndEstado(idPersona, estadoEntidad);
    }
}