package pe.edu.unsa.sisprestamos.repositorio.impl;

import pe.edu.unsa.sisprestamos.dominio.politicas_sanciones.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.repositorio.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador.PoliticasSancionesMapeador;
import pe.edu.unsa.sisprestamos.repositorio.politicas_sanciones.IHistorialRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional
public class HistorialPrestamoRepositorioImpl implements IHistorialRepositorio {

    private final HistorialPrestamoJpaRepositorio historialJpaRepositorio;
    private final PoliticasSancionesMapeador mapeador;

    @Override
    public HistorialPrestamo guardar(HistorialPrestamo historial) {
        HistorialPrestamoEntidad entidad = mapeador.aEntidad(historial);
        HistorialPrestamoEntidad guardada = historialJpaRepositorio.save(entidad);
        return mapeador.aDominio(guardada);
    }

    @Override
    public List<HistorialPrestamo> buscarPorPersona(Long idPersona) {
        return historialJpaRepositorio.findByPersonaIdPersona(idPersona)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<HistorialPrestamo> buscarPorPrestamo(Long idPrestamo) {
        return historialJpaRepositorio.findByPrestamoIdPrestamo(idPrestamo)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<HistorialPrestamo> buscarPorPersonaOrdenadoPorFechaDesc(Long idPersona) {
        return historialJpaRepositorio.findByPersonaIdPersonaOrderByFechaEventoDesc(idPersona)
                .stream()
                .map(mapeador::aDominio)
                .collect(Collectors.toList());
    }
}