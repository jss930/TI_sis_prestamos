package com.universidad.biblioteca.dominio.repositorios;

import pe.edu.unsa.sisprestamos.dominio.catalogo.*;
import pe.edu.unsa.sisprestamos.dominio.compartido.Identificador;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de Items (Puerto de salida).
 */
public interface IItemRepositorio {

    Item guardar(Item item);

    Optional<Item> buscarPorId(Long id);

    List<Item> buscarPorCategoria(Item.Categoria categoria);

    List<Item> buscarPorTitulo(String titulo);

    List<Item> buscarDisponibles();

    List<Item> buscarTodosLosLibros();

    List<Item> buscarTodosLosEquipos();

    void eliminarPorId(Long id);
}