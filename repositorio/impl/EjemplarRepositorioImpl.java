package pe.edu.unsa.sisprestamos.repositorio.impl;

import pe.edu.unsa.sisprestamos.dominio.catalogo.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador.CatalogoMapeador;
import pe.edu.unsa.sisprestamos.repositorio.catalogo.IEjemplarRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional
public class EjemplarRepositorioImpl implements IEjemplarRepositorio {

    private final EjemplarJpaRepositorio ejemplarJpaRepositorio;
    private final CatalogoMapeador mapeador;

    @Override
    public Ejemplar guardar(Ejemplar ejemplar) {
        EjemplarEntidad entidad = mapeador.aEntidad(ejemplar);
        EjemplarEntidad guardada = ejemplarJpaRepositorio.save(entidad);
        return mapeador.aDominio(guardada);
    }

    @Override
    public Optional<Ejemplar> buscarPorId(Long id) {
        return ejemplarJpaRepositorio.findById(id)
                .map(mapeador::aDominio);
    }

    @Override
    public Optional<Ejemplar> buscarPorCodigoInventario(String codigoInventario) {
        return ejemplarJpaRepositorio.findByCodigoInventario(codigoInventario)
                .map(mapeador::aDominio);
    }

    @Override
    public List<Ejemplar> buscarPorItem(Long idItem) {
        return ejemplarJpaRepositorio.findByItemIdItem(idItem)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ejemplar> buscarDisponiblesPorItem(Long idItem) {
        return ejemplarJpaRepositorio.findDisponiblesByItem(idItem)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ejemplar> buscarPorEstadoFisico(Ejemplar.EstadoFisico estadoFisico) {
        EjemplarEntidad.EstadoFisico estadoEntidad = EjemplarEntidad.EstadoFisico.valueOf(estadoFisico.name());
        return ejemplarJpaRepositorio.findByEstadoFisico(estadoEntidad)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPorId(Long id) {
        ejemplarJpaRepositorio.deleteById(id);
    }
}