package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.model.DetalleItem;
import com.biblioteca.catalog.domain.model.EquipoDetalle;
import com.biblioteca.catalog.domain.model.LibroDetalle;
import com.biblioteca.catalog.domain.model.OtroDetalle;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.Autor;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.ISBN;
import com.biblioteca.catalog.domain.vo.Titulo;
import com.biblioteca.infrastructure.persistence.jpa.entity.CatalogoItemJpaEntity;
import com.biblioteca.infrastructure.persistence.jpa.repository.CatalogoItemJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador JPA del puerto de catalogo. */
@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "jpa", matchIfMissing = true)
public class CatalogoItemJpaAdapter implements CatalogoItemRepository {

    private final CatalogoItemJpaRepository repositorio;

    public CatalogoItemJpaAdapter(CatalogoItemJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public CatalogoItem guardar(CatalogoItem item) {
        repositorio.save(aEntidad(item));
        return item;
    }

    @Override
    public Optional<CatalogoItem> buscarPorId(CatalogoItemId id) {
        return repositorio.findById(id.value()).map(CatalogoItemJpaAdapter::aDominio);
    }

    @Override
    public List<CatalogoItem> listarTodos() {
        return repositorio.findAll().stream().map(CatalogoItemJpaAdapter::aDominio).toList();
    }

    @Override
    public boolean existePorIsbn(ISBN isbn) {
        return repositorio.existsByIsbn(isbn.value());
    }

    private static CatalogoItemJpaEntity aEntidad(CatalogoItem item) {
        CatalogoItemJpaEntity entidad = new CatalogoItemJpaEntity();
        entidad.id = item.id().value();
        entidad.titulo = item.titulo().value();
        entidad.tipo = item.tipo();
        entidad.activo = item.activo();
        entidad.fechaAlta = item.fechaAlta();

        switch (item.detalle()) {
            case LibroDetalle libro -> {
                entidad.isbn = libro.isbn().value();
                entidad.autor = libro.autor().value();
                entidad.editorial = libro.editorial();
                entidad.anioPublicacion = libro.anioPublicacion();
            }
            case EquipoDetalle equipo -> {
                entidad.marca = equipo.marca();
                entidad.modelo = equipo.modelo();
            }
            case OtroDetalle otro -> entidad.descripcion = otro.descripcion();
        }
        return entidad;
    }

    private static CatalogoItem aDominio(CatalogoItemJpaEntity entidad) {
        DetalleItem detalle = switch (entidad.tipo) {
            case LIBRO -> new LibroDetalle(new ISBN(entidad.isbn), new Autor(entidad.autor),
                    entidad.editorial, entidad.anioPublicacion);
            case EQUIPO -> new EquipoDetalle(entidad.marca, entidad.modelo);
            case OTRO -> new OtroDetalle(entidad.descripcion);
        };
        return CatalogoItem.reconstruir(new CatalogoItemId(entidad.id),
                new Titulo(entidad.titulo), entidad.tipo, detalle, entidad.activo, entidad.fechaAlta);
    }
}
