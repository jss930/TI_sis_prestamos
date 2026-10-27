package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.circulation.domain.model.InspeccionDevolucion;
import com.biblioteca.circulation.domain.repository.InspeccionDevolucionRepository;
import com.biblioteca.infrastructure.persistence.jpa.entity.InspeccionDevolucionJpaEntity;
import com.biblioteca.infrastructure.persistence.jpa.repository.InspeccionDevolucionJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "jpa", matchIfMissing = true)
public class InspeccionDevolucionJpaAdapter implements InspeccionDevolucionRepository {
    private final InspeccionDevolucionJpaRepository repositorio;
    public InspeccionDevolucionJpaAdapter(InspeccionDevolucionJpaRepository repositorio) {
        this.repositorio = repositorio;
    }
    public void guardar(InspeccionDevolucion inspeccion) {
        var e = new InspeccionDevolucionJpaEntity();
        e.prestamoId = inspeccion.prestamoId();
        e.recibidoPorId = inspeccion.recibidoPorId();
        e.estadoFisico = inspeccion.estadoFisico();
        e.accesoriosFaltantes = inspeccion.accesoriosFaltantes();
        e.observaciones = inspeccion.observaciones();
        e.fecha = inspeccion.fecha();
        repositorio.save(e);
    }
    public List<InspeccionDevolucion> listar() {
        return repositorio.findAllByOrderByFechaDesc().stream().map(e -> new InspeccionDevolucion(
                e.prestamoId, e.recibidoPorId, e.estadoFisico, e.accesoriosFaltantes, e.observaciones, e.fecha)).toList();
    }
}
