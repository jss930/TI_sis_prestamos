package com.universidad.biblioteca.infraestructura.persistencia;

import pe.edu.unsa.sisprestamos.dominio.catalogo.Item;
import pe.edu.unsa.sisprestamos.dominio.politicas_sanciones.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador.PoliticasSancionesMapeador;
import pe.edu.unsa.sisprestamos.repositorio.politicas_sanciones.IPoliticaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional
public class PoliticaPrestamoRepositorioImpl implements IPoliticaRepositorio {

    private final PoliticaPrestamoJpaRepositorio politicaJpaRepositorio;
    private final PoliticasSancionesMapeador mapeador;

    @Override
    public PoliticaPrestamo guardar(PoliticaPrestamo politica) {
        PoliticaPrestamoEntidad entidad = mapeador.aEntidad(politica);
        PoliticaPrestamoEntidad guardada = politicaJpaRepositorio.save(entidad);
        return mapeador.aDominio(guardada);
    }

    @Override
    public Optional<PoliticaPrestamo> buscarPorId(Long id) {
        return politicaJpaRepositorio.findById(id)
                .map(mapeador::aDominio);
    }

    @Override
    public Optional<PoliticaPrestamo> buscarPorTipoPersonaYTipoItem(String tipoPersona, Item.Categoria tipoItem) {
        ItemEntidad.CategoriaItem categoriaEntidad = ItemEntidad.CategoriaItem.valueOf(tipoItem.name());
        return politicaJpaRepositorio.findByTipoPersonaAndTipoItem(tipoPersona, categoriaEntidad)
                .map(mapeador::aDominio);
    }

    @Override
    public List<PoliticaPrestamo> buscarPorTipoPersona(String tipoPersona) {
        return politicaJpaRepositorio.findByTipoPersona(tipoPersona)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }
}