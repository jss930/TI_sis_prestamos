package pe.edu.unsa.sisprestamos.repositorio.impl;

import pe.edu.unsa.sisprestamos.dominio.politicas_sanciones.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador.PoliticasSancionesMapeador;
import pe.edu.unsa.sisprestamos.repositorio.politicas_sanciones.ISancionRepositorio;
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
public class SancionRepositorioImpl implements ISancionRepositorio {

    private final SancionJpaRepositorio sancionJpaRepositorio;
    private final PoliticasSancionesMapeador mapeador;

    @Override
    public Sancion guardar(Sancion sancion) {
        SancionEntidad entidad = mapeador.aEntidad(sancion);
        SancionEntidad guardada = sancionJpaRepositorio.save(entidad);
        return mapeador.aDominio(guardada);
    }

    @Override
    public Optional<Sancion> buscarPorId(Long id) {
        return sancionJpaRepositorio.findById(id)
                .map(mapeador::aDominio);
    }

    @Override
    public List<Sancion> buscarPorPersona(Long idPersona) {
        return sancionJpaRepositorio.findByPersonaIdPersona(idPersona)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Sancion> buscarPorPrestamo(Long idPrestamo) {
        return sancionJpaRepositorio.findByPrestamoIdPrestamo(idPrestamo)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Sancion> buscarPorEstado(Sancion.Estado estado) {
        SancionEntidad.EstadoSancion estadoEntidad = SancionEntidad.EstadoSancion.valueOf(estado.name());
        return sancionJpaRepositorio.findByEstado(estadoEntidad)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Sancion> buscarActivasPorPersona(Long idPersona) {
        return sancionJpaRepositorio.findActivasByPersona(idPersona)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Sancion> buscarVencidas(OffsetDateTime ahora) {
        return sancionJpaRepositorio.findVencidas(ahora)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Sancion> buscarPorPrestamoYMotivo(Long idPrestamo, Sancion.Motivo motivo) {
        SancionEntidad.MotivoSancion motivoEntidad = SancionEntidad.MotivoSancion.valueOf(motivo.name());
        return sancionJpaRepositorio.findByPrestamoIdPrestamoAndMotivo(idPrestamo, motivoEntidad)
                .map(mapeador::aDominio);
    }
}