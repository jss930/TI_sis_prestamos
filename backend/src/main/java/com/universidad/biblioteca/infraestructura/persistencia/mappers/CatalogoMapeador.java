package com.universidad.biblioteca.infraestructura.persistencia.mappers;

import pe.edu.unsa.sisprestamos.dominio.catalogo.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CatalogoMapeador {

    CatalogoMapeador INSTANCE = Mappers.getMapper(CatalogoMapeador.class);

    @Mapping(target = "idUbicacion", source = "idUbicacion")
    @Mapping(target = "sala", source = "sala")
    @Mapping(target = "estante", source = "estante")
    UbicacionFisica aDominio(UbicacionFisicaEntidad entidad);

    @Mapping(target = "idUbicacion", source = "idUbicacion")
    @Mapping(target = "sala", source = "sala")
    @Mapping(target = "estante", source = "estante")
    UbicacionFisicaEntidad aEntidad(UbicacionFisica dominio);

    @Mapping(target = "idItem", source = "idItem")
    @Mapping(target = "titulo", source = "titulo")
    @Mapping(target = "categoria", source = "categoria")
    @Mapping(target = "cantidadTotal", source = "cantidadTotal")
    @Mapping(target = "cantidadDisponible", source = "cantidadDisponible")
    Item aDominio(ItemEntidad entidad);

    @Mapping(target = "idItem", source = "idItem")
    @Mapping(target = "titulo", source = "titulo")
    @Mapping(target = "categoria", source = "categoria")
    @Mapping(target = "cantidadTotal", source = "cantidadTotal")
    @Mapping(target = "cantidadDisponible", source = "cantidadDisponible")
    ItemEntidad aEntidad(Item dominio);

    @Mapping(target = "idItem", source = "idItem")
    @Mapping(target = "isbn", source = "isbn")
    @Mapping(target = "autor", source = "autor")
    @Mapping(target = "editorial", source = "editorial")
    @Mapping(target = "edicion", source = "edicion")
    Libro aDominio(LibroEntidad entidad);

    @Mapping(target = "idItem", source = "idItem")
    @Mapping(target = "isbn", source = "isbn")
    @Mapping(target = "autor", source = "autor")
    @Mapping(target = "editorial", source = "editorial")
    @Mapping(target = "edicion", source = "edicion")
    LibroEntidad aEntidad(Libro dominio);

    @Mapping(target = "idItem", source = "idItem")
    @Mapping(target = "numeroSerie", source = "numeroSerie")
    @Mapping(target = "marca", source = "marca")
    @Mapping(target = "modelo", source = "modelo")
    Equipo aDominio(EquipoEntidad entidad);

    @Mapping(target = "idItem", source = "idItem")
    @Mapping(target = "numeroSerie", source = "numeroSerie")
    @Mapping(target = "marca", source = "marca")
    @Mapping(target = "modelo", source = "modelo")
    EquipoEntidad aEntidad(Equipo dominio);

    @Mapping(target = "idEjemplar", source = "idEjemplar")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "ubicacion", source = "ubicacion")
    @Mapping(target = "codigoInventario", source = "codigoInventario")
    @Mapping(target = "estadoFisico", source = "estadoFisico")
    @Mapping(target = "disponible", source = "disponible")
    Ejemplar aDominio(EjemplarEntidad entidad);

    @Mapping(target = "idEjemplar", source = "idEjemplar")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "ubicacion", source = "ubicacion")
    @Mapping(target = "codigoInventario", source = "codigoInventario")
    @Mapping(target = "estadoFisico", source = "estadoFisico")
    @Mapping(target = "disponible", source = "disponible")
    EjemplarEntidad aEntidad(Ejemplar dominio);

    List<UbicacionFisica> aDominioLista(List<UbicacionFisicaEntidad> entidades);
    List<Item> aDominioLista(List<ItemEntidad> entidades);
    List<Libro> aDominioLista(List<LibroEntidad> entidades);
    List<Equipo> aDominioLista(List<EquipoEntidad> entidades);
    List<Ejemplar> aDominioLista(List<EjemplarEntidad> entidades);
}