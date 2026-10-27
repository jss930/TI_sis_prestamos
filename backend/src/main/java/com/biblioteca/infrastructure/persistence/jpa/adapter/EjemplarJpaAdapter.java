package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.infrastructure.persistence.jpa.entity.EjemplarJpaEntity;
import com.biblioteca.infrastructure.persistence.jpa.repository.EjemplarJpaRepository;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.model.Ubicacion;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto de inventario. */
@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "jpa", matchIfMissing = true)
public class EjemplarJpaAdapter implements EjemplarRepository {

    private final EjemplarJpaRepository repositorio;

    public EjemplarJpaAdapter(EjemplarJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Ejemplar guardar(Ejemplar ejemplar) {
        repositorio.save(aEntidad(ejemplar));
        return ejemplar;
    }

    @Override
    public Optional<Ejemplar> buscarPorId(EjemplarId id) {
        return repositorio.findById(id.value()).map(EjemplarJpaAdapter::aDominio);
    }

    @Override
    public Optional<Ejemplar> buscarPorBarcode(Barcode barcode) {
        return repositorio.findByBarcode(barcode.value()).map(EjemplarJpaAdapter::aDominio);
    }

    @Override
    public Optional<Ejemplar> buscarPorIdParaActualizar(EjemplarId id) {
        return repositorio.bloquearPorId(id.value()).map(EjemplarJpaAdapter::aDominio);
    }

    @Override
    public List<Ejemplar> listarTodos() {
        return repositorio.findAll().stream().map(EjemplarJpaAdapter::aDominio).toList();
    }

    @Override
    public List<Ejemplar> listarPorItem(CatalogoItemId catalogoItemId) {
        return repositorio.findByCatalogoItemId(catalogoItemId.value()).stream()
                .map(EjemplarJpaAdapter::aDominio).toList();
    }

    @Override
    public boolean existePorBarcode(Barcode barcode) {
        return repositorio.existsByBarcode(barcode.value());
    }

    private static EjemplarJpaEntity aEntidad(Ejemplar ejemplar) {
        EjemplarJpaEntity entidad = new EjemplarJpaEntity();
        entidad.id = ejemplar.id().value();
        entidad.catalogoItemId = ejemplar.catalogoItemId().value();
        entidad.barcode = ejemplar.barcode().value();
        entidad.sala = ejemplar.ubicacion().sala();
        entidad.estante = ejemplar.ubicacion().estante();
        entidad.estadoFisico = ejemplar.estadoFisico();
        entidad.notas = ejemplar.notas();
        entidad.fechaAlta = ejemplar.fechaAlta();
        return entidad;
    }

    private static Ejemplar aDominio(EjemplarJpaEntity entidad) {
        return Ejemplar.reconstruir(new EjemplarId(entidad.id),
                new CatalogoItemId(entidad.catalogoItemId), new Barcode(entidad.barcode),
                new Ubicacion(entidad.sala, entidad.estante), entidad.estadoFisico,
                entidad.notas, entidad.fechaAlta);
    }
}
