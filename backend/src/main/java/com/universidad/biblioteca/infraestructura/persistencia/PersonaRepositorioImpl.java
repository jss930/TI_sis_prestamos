package com.universidad.biblioteca.infraestructura.persistencia;

import pe.edu.unsa.sisprestamos.dominio.usuarios.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador.PersonaMapeador;
import pe.edu.unsa.sisprestamos.repositorio.usuarios.IPersonaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional
public class PersonaRepositorioImpl implements IPersonaRepositorio {

    private final PersonaJpaRepositorio personaJpaRepositorio;
    private final AdminJpaRepositorio adminJpaRepositorio;
    private final PerfilJpaRepositorio perfilJpaRepositorio;
    private final AlumnoJpaRepositorio alumnoJpaRepositorio;
    private final DocenteJpaRepositorio docenteJpaRepositorio;
    private final AdministrativoJpaRepositorio administrativoJpaRepositorio;
    private final PersonaMapeador mapeador;

    @Override
    public Persona guardar(Persona persona) {
        PersonaEntidad entidad = mapeador.aEntidad(persona);
        
        // Guardar la persona base
        PersonaEntidad guardada = personaJpaRepositorio.save(entidad);
        
        // Guardar las especializaciones si existen
        if (persona.getAdmin() != null) {
            AdminEntidad adminEntidad = mapeador.aEntidad(persona.getAdmin());
            adminEntidad.setPersona(guardada);
            adminJpaRepositorio.save(adminEntidad);
        }
        
        if (persona.getPerfil() != null) {
            PerfilEntidad perfilEntidad = mapeador.aEntidad(persona.getPerfil());
            perfilEntidad.setPersona(guardada);
            perfilJpaRepositorio.save(perfilEntidad);
        }
        
        if (persona.getAlumno() != null) {
            AlumnoEntidad alumnoEntidad = mapeador.aEntidad(persona.getAlumno());
            alumnoEntidad.setPersona(guardada);
            alumnoJpaRepositorio.save(alumnoEntidad);
        }
        
        if (persona.getDocente() != null) {
            DocenteEntidad docenteEntidad = mapeador.aEntidad(persona.getDocente());
            docenteEntidad.setPersona(guardada);
            docenteJpaRepositorio.save(docenteEntidad);
        }
        
        if (persona.getAdministrativo() != null) {
            AdministrativoEntidad adminEntidad = mapeador.aEntidad(persona.getAdministrativo());
            adminEntidad.setPersona(guardada);
            administrativoJpaRepositorio.save(adminEntidad);
        }
        
        return mapeador.aDominio(guardada);
    }

    @Override
    public Optional<Persona> buscarPorId(Long id) {
        return personaJpaRepositorio.findById(id)
                .map(mapeador::aDominio);
    }

    @Override
    public Optional<Persona> buscarPorCorreo(String correo) {
        return personaJpaRepositorio.findByContactoCorreo(correo)
                .map(mapeador::aDominio);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return personaJpaRepositorio.existsByContactoCorreo(correo);
    }

    @Override
    public List<Persona> buscarPorEstado(Persona.Estado estado) {
        PersonaEntidad.EstadoPersona estadoEntidad = PersonaEntidad.EstadoPersona.valueOf(estado.name());
        return personaJpaRepositorio.findByEstado(estadoEntidad)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Persona> buscarPorNombreOApellido(String termino) {
        return personaJpaRepositorio.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(termino, termino)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Persona> obtenerTodosLosAdmins() {
        return personaJpaRepositorio.findAllAdmins()
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Persona> obtenerTodosLosAlumnos() {
        return personaJpaRepositorio.findAllAlumnos()
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Persona> obtenerTodosLosDocentes() {
        return personaJpaRepositorio.findAllDocentes()
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Persona> obtenerTodosLosAdministrativos() {
        return personaJpaRepositorio.findAllAdministrativos()
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPorId(Long id) {
        personaJpaRepositorio.deleteById(id);
    }
}