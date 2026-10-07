package com.universidad.biblioteca.dominio.repositorios;

import pe.edu.unsa.sisprestamos.dominio.catalogo.Item;
import pe.edu.unsa.sisprestamos.dominio.politicas_sanciones.*;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de Políticas de Préstamo.
 */
public interface IPoliticaRepositorio {

    PoliticaPrestamo guardar(PoliticaPrestamo politica);

    Optional<PoliticaPrestamo> buscarPorId(Long id);

    Optional<PoliticaPrestamo> buscarPorTipoPersonaYTipoItem(String tipoPersona, Item.Categoria tipoItem);

    List<PoliticaPrestamo> buscarPorTipoPersona(String tipoPersona);
}