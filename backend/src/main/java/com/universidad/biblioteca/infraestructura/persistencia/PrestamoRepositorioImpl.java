package com.universidad.biblioteca.infraestructura.persistencia;

import pe.edu.unsa.sisprestamos.dominio.circulacion.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador.CirculacionMapeador;
import pe.edu.unsa.sisprestamos.repositorio.circulacion.IPrestamoRepositorio;
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
public class PrestamoRepositorioImpl implements IPrestamoRepositorio {

    private final PrestamoJpaRepositorio prestamoJpaRepositorio;
    private final CirculacionMapeador mapeador;

    @Override
    public Prestamo guardar(Prestamo prestamo) {
        PrestamoEntidad entidad = mapeador.aEntidad(prestamo);
        PrestamoEntidad guardada = prestamoJpaRepositorio.save(entidad);
        return mapeador.aDominio(guardada);
    }

    @Override
    public Optional<Prestamo> buscarPorId(Long id) {
        return prestamoJpaRepositorio.findById(id)
                .map(mapeador::aDominio);
    }

    @Override
    public List<Prestamo> buscarPorPersona(Long idPersona) {
        return prestamoJpaRepositorio.findByPersonaIdPersona(idPersona)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Prestamo> buscarPorEjemplar(Long idEjemplar) {
        return prestamoJpaRepositorio.findByEjemplarIdEjemplar(idEjemplar)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Prestamo> buscarPorEstado(Prestamo.Estado estado) {
        PrestamoEntidad.EstadoPrestamo estadoEntidad = PrestamoEntidad.EstadoPrestamo.valueOf(estado.name());
        return prestamoJpaRepositorio.findByEstado(estadoEntidad)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Prestamo> buscarActivosPorPersona(Long idPersona) {
        return prestamoJpaRepositorio.findActivosByPersona(idPersona)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Prestamo> buscarVencidos(OffsetDateTime ahora) {
        return prestamoJpaRepositorio.findVencidos(ahora)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Prestamo> buscarActivoPorEjemplar(Long idEjemplar) {
        return prestamoJpaRepositorio.findActivoByEjemplar(idEjemplar)
                .map(mapeador::aDominio);
    }

    @Override
    public long contarPorPersonaYEstado(Long idPersona, Prestamo.Estado estado) {
        PrestamoEntidad.EstadoPrestamo estadoEntidad = PrestamoEntidad.EstadoPrestamo.valueOf(estado.name());
        return prestamoJpaRepositorio.countByPersonaIdPersonaAndEstado(idPersona, estadoEntidad);
    }
}