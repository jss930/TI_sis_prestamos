package com.universidad.biblioteca.dominio.item;

import java.util.Objects;
import java.util.UUID;

/**
 * Raíz del agregado Item.
 * Libro y Equipo la extienden; la categoría es derivada (/categoria en el UML).
 */
public class Item {

    private final UUID idItem;
    private String titulo;
    private Ubicacion ubicacion;

    private int cantidadTotal;
    private int cantidadDisponible;
    private int cantidadDanada;
    private int cantidadPerdida;

    /** Constructor para crear un ítem nuevo (sin unidades todavía). */
    protected Item(String titulo, Ubicacion ubicacion) {
        this(UUID.randomUUID(), titulo, ubicacion, 0, 0, 0, 0);
    }

    /** Constructor completo, también lo usa el mapper al reconstruir desde la BD. */
    protected Item(UUID idItem, String titulo, Ubicacion ubicacion,
                   int cantidadTotal, int cantidadDisponible,
                   int cantidadDanada, int cantidadPerdida) {
        this.idItem = Objects.requireNonNull(idItem, "idItem es obligatorio");
        this.titulo = validarTitulo(titulo);
        this.ubicacion = Objects.requireNonNull(ubicacion, "ubicacion es obligatoria");
        this.cantidadTotal = cantidadTotal;
        this.cantidadDisponible = cantidadDisponible;
        this.cantidadDanada = cantidadDanada;
        this.cantidadPerdida = cantidadPerdida;
        validarInvariante();
    }

    /** Agrega unidades nuevas al inventario; quedan disponibles. */
    public void registrarUnidades(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a registrar debe ser mayor a 0");
        }
        cantidadTotal += cantidad;
        cantidadDisponible += cantidad;
    }

    /** Una unidad sale en préstamo. */
    public void prestar() {
        if (cantidadDisponible <= 0) {
            throw new IllegalStateException("No hay unidades disponibles de: " + titulo);
        }
        cantidadDisponible--;
    }

    /** Una unidad prestada vuelve en buen estado. */
    public void recibirDevolucion() {
        exigirUnidadPrestada();
        cantidadDisponible++;
    }

    /** Una unidad prestada vuelve dañada: no queda disponible. */
    public void marcarDanada() {
        exigirUnidadPrestada();
        cantidadDanada++;
    }

    /** Una unidad prestada no se devuelve: se da por perdida. */
    public void marcarPerdida() {
        exigirUnidadPrestada();
        cantidadPerdida++;
    }

    /** Categoría derivada: las subclases la sobrescriben. */
    public CategoriaItem getCategoria() {
        return CategoriaItem.OTRO;
    }

    public boolean estaDisponible() {
        return cantidadDisponible > 0;
    }

    /** Unidades que están actualmente en préstamo. */
    public int getCantidadPrestada() {
        return cantidadTotal - cantidadDisponible - cantidadDanada - cantidadPerdida;
    }

    private void exigirUnidadPrestada() {
        if (getCantidadPrestada() <= 0) {
            throw new IllegalStateException("No hay unidades prestadas de: " + titulo);
        }
    }

    private void validarInvariante() {
        if (cantidadTotal < 0 || cantidadDisponible < 0 || cantidadDanada < 0 || cantidadPerdida < 0) {
            throw new IllegalArgumentException("Las cantidades no pueden ser negativas");
        }
        if (cantidadDisponible + cantidadDanada + cantidadPerdida > cantidadTotal) {
            throw new IllegalArgumentException("Las cantidades superan el total del ítem");
        }
    }

    private static String validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        return titulo.trim();
    }

    public UUID getIdItem() { return idItem; }
    public String getTitulo() { return titulo; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public int getCantidadTotal() { return cantidadTotal; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public int getCantidadDanada() { return cantidadDanada; }
    public int getCantidadPerdida() { return cantidadPerdida; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Item otro && idItem.equals(otro.idItem));
    }

    @Override
    public int hashCode() {
        return idItem.hashCode();
    }
}