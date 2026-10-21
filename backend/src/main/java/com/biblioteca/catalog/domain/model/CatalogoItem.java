package com.biblioteca.catalog.domain.model;

import com.biblioteca.catalog.domain.event.ItemCatalogoRegistradoEvent;
import com.biblioteca.catalog.domain.vo.Autor;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.ISBN;
import com.biblioteca.catalog.domain.vo.Titulo;
import com.biblioteca.shared.domain.AggregateRoot;
import com.biblioteca.shared.domain.DomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * Agregado de catalogo: describe QUE es un item.
 *
 * <p>No conoce cantidades ni disponibilidad. Una copia fisica es un
 * {@code Ejemplar} del contexto inventory.</p>
 */
public final class CatalogoItem extends AggregateRoot<CatalogoItemId> {

    private final CatalogoItemId id;
    private final TipoItem tipo;
    private final DetalleItem detalle;
    private final Instant fechaAlta;
    private Titulo titulo;
    private boolean activo;

    private CatalogoItem(CatalogoItemId id, Titulo titulo, TipoItem tipo,
                         DetalleItem detalle, Instant fechaAlta, boolean activo) {
        this.id = Objects.requireNonNull(id);
        this.titulo = Objects.requireNonNull(titulo);
        this.tipo = Objects.requireNonNull(tipo);
        this.detalle = Objects.requireNonNull(detalle);
        this.fechaAlta = Objects.requireNonNull(fechaAlta);
        this.activo = activo;
        if (detalle.tipo() != tipo) {
            throw new DomainException("DETALLE_INCOMPATIBLE",
                    "El detalle no corresponde al tipo de item " + tipo);
        }
    }

    public static CatalogoItem registrarLibro(Titulo titulo, ISBN isbn, Autor autor,
                                              String editorial, int anioPublicacion) {
        CatalogoItem item = new CatalogoItem(CatalogoItemId.nuevo(), titulo, TipoItem.LIBRO,
                new LibroDetalle(isbn, autor, editorial, anioPublicacion), Instant.now(), true);
        item.registrarEvento(new ItemCatalogoRegistradoEvent(
                item.id, item.titulo, item.tipo, item.fechaAlta));
        return item;
    }

    public static CatalogoItem registrarEquipo(Titulo titulo, String marca, String modelo) {
        CatalogoItem item = new CatalogoItem(CatalogoItemId.nuevo(), titulo, TipoItem.EQUIPO,
                new EquipoDetalle(marca, modelo), Instant.now(), true);
        item.registrarEvento(new ItemCatalogoRegistradoEvent(
                item.id, item.titulo, item.tipo, item.fechaAlta));
        return item;
    }

    public static CatalogoItem registrarOtro(Titulo titulo, String descripcion) {
        CatalogoItem item = new CatalogoItem(CatalogoItemId.nuevo(), titulo, TipoItem.OTRO,
                new OtroDetalle(descripcion), Instant.now(), true);
        item.registrarEvento(new ItemCatalogoRegistradoEvent(
                item.id, item.titulo, item.tipo, item.fechaAlta));
        return item;
    }

    public static CatalogoItem reconstruir(CatalogoItemId id, Titulo titulo, TipoItem tipo,
                                           DetalleItem detalle, boolean activo, Instant fechaAlta) {
        return new CatalogoItem(id, titulo, tipo, detalle, fechaAlta, activo);
    }

    public void actualizarTitulo(Titulo nuevoTitulo) {
        this.titulo = Objects.requireNonNull(nuevoTitulo);
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    @Override
    public CatalogoItemId id() {
        return id;
    }

    public Titulo titulo() {
        return titulo;
    }

    public TipoItem tipo() {
        return tipo;
    }

    public DetalleItem detalle() {
        return detalle;
    }

    public Instant fechaAlta() {
        return fechaAlta;
    }

    public boolean activo() {
        return activo;
    }

    @Override
    public boolean equals(Object otro) {
        return this == otro || (otro instanceof CatalogoItem item && id.equals(item.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
