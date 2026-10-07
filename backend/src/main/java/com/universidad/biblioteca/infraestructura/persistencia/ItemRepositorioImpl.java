package com.universidad.biblioteca.infraestructura.persistencia;

import pe.edu.unsa.sisprestamos.dominio.catalogo.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador.CatalogoMapeador;
import pe.edu.unsa.sisprestamos.repositorio.catalogo.IItemRepositorio;
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
public class ItemRepositorioImpl implements IItemRepositorio {

    private final ItemJpaRepositorio itemJpaRepositorio;
    private final LibroJpaRepositorio libroJpaRepositorio;
    private final EquipoJpaRepositorio equipoJpaRepositorio;
    private final CatalogoMapeador mapeador;

    @Override
    public Item guardar(Item item) {
        ItemEntidad entidad = mapeador.aEntidad(item);
        ItemEntidad guardada = itemJpaRepositorio.save(entidad);
        
        if (item instanceof Libro libro) {
            LibroEntidad libroEntidad = mapeador.aEntidad(libro);
            libroEntidad.setItem(guardada);
            libroJpaRepositorio.save(libroEntidad);
        } else if (item instanceof Equipo equipo) {
            EquipoEntidad equipoEntidad = mapeador.aEntidad(equipo);
            equipoEntidad.setItem(guardada);
            equipoJpaRepositorio.save(equipoEntidad);
        }
        
        return mapeador.aDominio(guardada);
    }

    @Override
    public Optional<Item> buscarPorId(Long id) {
        return itemJpaRepositorio.findById(id)
                .map(mapeador::aDominio);
    }

    @Override
    public List<Item> buscarPorCategoria(Item.Categoria categoria) {
        ItemEntidad.CategoriaItem categoriaEntidad = ItemEntidad.CategoriaItem.valueOf(categoria.name());
        return itemJpaRepositorio.findByCategoria(categoriaEntidad)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> buscarPorTitulo(String titulo) {
        return itemJpaRepositorio.findByTituloContainingIgnoreCase(titulo)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> buscarDisponibles() {
        return itemJpaRepositorio.findDisponibles()
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> buscarTodosLosLibros() {
        return itemJpaRepositorio.findAllLibros()
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> buscarTodosLosEquipos() {
        return itemJpaRepositorio.findAllEquipos()
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPorId(Long id) {
        itemJpaRepositorio.deleteById(id);
    }
}