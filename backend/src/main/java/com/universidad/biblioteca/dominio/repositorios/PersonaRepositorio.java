package com.universidad.biblioteca.dominio.repositorios;

import com.universidad.biblioteca.dominio.persona.EstadoCuenta;
import com.universidad.biblioteca.dominio.persona.Persona;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de Personas (Puerto de salida).
 * Define el contrato para la persistencia de personas.
 */
public interface PersonaRepositorio {

    /**
     * Guarda una persona nueva o actualiza una existente.
     */
    Persona guardar(Persona persona);

    /**
     * Busca una persona por su ID.
     */
    Optional<Persona> buscarPorId(Long id);

    /**
     * Busca una persona por su correo electrónico.
     */
    Optional<Persona> buscarPorCorreo(String correo);

    /**
     * Verifica si existe una persona con el correo dado.
     */
    boolean existePorCorreo(String correo);

    /**
     * Busca todas las personas con un estado específico.
     */
    List<Persona> buscarPorEstado(EstadoCuenta estado);

    /**
     * Busca personas por nombre o apellido (búsqueda parcial, case-insensitive).
     */
    List<Persona> buscarPorNombreOApellido(String termino);

    /**
     * Obtiene todas las personas que son administradores.
     */
    List<Persona> obtenerTodosLosAdmins();

    /**
     * Obtiene todas las personas que son alumnos.
     */
    List<Persona> obtenerTodosLosAlumnos();

    /**
     * Obtiene todas las personas que son docentes.
     */
    List<Persona> obtenerTodosLosDocentes();

    /**
     * Obtiene todas las personas que son administrativos.
     */
    List<Persona> obtenerTodosLosAdministrativos();

    /**
     * Elimina una persona por su ID.
     */
    void eliminarPorId(Long id);
}