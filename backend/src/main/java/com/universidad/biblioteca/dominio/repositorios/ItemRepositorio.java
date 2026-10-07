package com.universidad.biblioteca.dominio.repositorios;

import com.universidad.biblioteca.dominio.item.CategoriaItem;
import com.universidad.biblioteca.dominio.item.Item;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de Items (Puerto de salida).
 */
public interface ItemRepositorio {

    Item guardar(Item item);

    Optional<Item> buscarPorId(Long id);

    List<Item> buscarPorCategoria(CategoriaItem categoria);

    List<Item> buscarPorTitulo(String titulo);

    List<Item> buscarDisponibles();

    List<Item> buscarTodosLosLibros();

    List<Item> buscarTodosLosEquipos();

    void eliminarPorId(Long id);
}