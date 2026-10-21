package com.biblioteca.inventory.domain.model;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.inventory.domain.event.EjemplarDevueltoEvent;
import com.biblioteca.inventory.domain.event.EjemplarPrestadoEvent;
import com.biblioteca.inventory.domain.event.EjemplarRegistradoEvent;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.AggregateRoot;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;

import java.time.Instant;
import java.util.Objects;

/**
 * Unidad fisica prestable: un libro, una laptop, un set.
 *
 * <p>Es un agregado independiente del catalogo para que dos prestamos de
 * unidades distintas no compitan por el mismo lock.</p>
 */
public final class Ejemplar extends AggregateRoot<EjemplarId> {

    private final EjemplarId id;
    private final CatalogoItemId catalogoItemId;
    private final Barcode barcode;
    private final Ubicacion ubicacion;
    private final Instant fechaAlta;
    private EstadoFisico estadoFisico;
    private String notas;

    private Ejemplar(EjemplarId id, CatalogoItemId catalogoItemId, Barcode barcode,
                     Ubicacion ubicacion, EstadoFisico estadoFisico, String notas, Instant fechaAlta) {
        this.id = Objects.requireNonNull(id);
        this.catalogoItemId = Objects.requireNonNull(catalogoItemId);
        this.barcode = Objects.requireNonNull(barcode);
        this.ubicacion = Objects.requireNonNull(ubicacion);
        this.estadoFisico = Objects.requireNonNull(estadoFisico);
        this.notas = normalizarNotas(notas);
        this.fechaAlta = Objects.requireNonNull(fechaAlta);
    }

    public static Ejemplar registrar(CatalogoItemId catalogoItemId, Barcode barcode, Ubicacion ubicacion) {
        return registrar(catalogoItemId, barcode, ubicacion, null);
    }

    public static Ejemplar registrar(CatalogoItemId catalogoItemId, Barcode barcode,
                                     Ubicacion ubicacion, String notas) {
        Ejemplar ejemplar = new Ejemplar(EjemplarId.nuevo(), catalogoItemId, barcode,
                ubicacion, EstadoFisico.DISPONIBLE, notas, Instant.now());
        ejemplar.registrarEvento(new EjemplarRegistradoEvent(
                ejemplar.id, catalogoItemId.value(), barcode, ejemplar.fechaAlta));
        return ejemplar;
    }

    public static Ejemplar reconstruir(EjemplarId id, CatalogoItemId catalogoItemId, Barcode barcode,
                                       Ubicacion ubicacion, EstadoFisico estadoFisico,
                                       String notas, Instant fechaAlta) {
        return new Ejemplar(id, catalogoItemId, barcode, ubicacion, estadoFisico, notas, fechaAlta);
    }

    public void prestar() {
        if (estadoFisico != EstadoFisico.DISPONIBLE) {
            throw new BusinessRuleException("EJEMPLAR_NO_DISPONIBLE",
                    "El ejemplar " + barcode.value() + " no esta disponible (estado " + estadoFisico + ")");
        }
        estadoFisico = EstadoFisico.PRESTADO;
        registrarEvento(new EjemplarPrestadoEvent(id, Instant.now()));
    }

    public void devolver(EstadoFisico estadoDevolucion) {
        if (estadoFisico != EstadoFisico.PRESTADO) {
            throw new BusinessRuleException("EJEMPLAR_NO_PRESTADO",
                    "El ejemplar " + barcode.value() + " no tiene un prestamo activo");
        }
        if (estadoDevolucion != EstadoFisico.DISPONIBLE
                && estadoDevolucion != EstadoFisico.DANADO
                && estadoDevolucion != EstadoFisico.PERDIDO) {
            throw new DomainException("ESTADO_DEVOLUCION_INVALIDO",
                    "La devolucion solo admite DISPONIBLE, DANADO o PERDIDO");
        }
        estadoFisico = estadoDevolucion;
        registrarEvento(new EjemplarDevueltoEvent(id, estadoDevolucion, Instant.now()));
    }

    public void darDeBaja(String motivo) {
        if (estadoFisico == EstadoFisico.PRESTADO) {
            throw new BusinessRuleException("EJEMPLAR_PRESTADO",
                    "No se puede dar de baja un ejemplar prestado");
        }
        estadoFisico = EstadoFisico.BAJA;
        this.notas = normalizarNotas(motivo);
    }

    public boolean estaDisponible() {
        return estadoFisico == EstadoFisico.DISPONIBLE;
    }

    private static String normalizarNotas(String notas) {
        return notas == null || notas.isBlank() ? null : notas.trim();
    }

    @Override
    public EjemplarId id() {
        return id;
    }

    public CatalogoItemId catalogoItemId() {
        return catalogoItemId;
    }

    public Barcode barcode() {
        return barcode;
    }

    public Ubicacion ubicacion() {
        return ubicacion;
    }

    public EstadoFisico estadoFisico() {
        return estadoFisico;
    }

    public String notas() {
        return notas;
    }

    public Instant fechaAlta() {
        return fechaAlta;
    }

    @Override
    public boolean equals(Object otro) {
        return this == otro || (otro instanceof Ejemplar ejemplar && id.equals(ejemplar.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
