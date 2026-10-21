package com.universidad.biblioteca.infraestructura.persistencia.mappers;

import pe.edu.unsa.sisprestamos.dominio.circulacion.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CirculacionMapeador {

    CirculacionMapeador INSTANCE = Mappers.getMapper(CirculacionMapeador.class);

    @Mapping(target = "idPrestamo", source = "idPrestamo")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "ejemplar", source = "ejemplar")
    @Mapping(target = "periodo.fechaInicio", source = "fechaInicio")
    @Mapping(target = "periodo.fechaVencimiento", source = "fechaVencimiento")
    @Mapping(target = "periodo.fechaDevolucionReal", source = "fechaDevolucionReal")
    @Mapping(target = "lugarUso", source = "lugarUso")
    @Mapping(target = "estado", source = "estado")
    Prestamo aDominio(PrestamoEntidad entidad);

    @Mapping(target = "idPrestamo", source = "idPrestamo")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "ejemplar", source = "ejemplar")
    @Mapping(target = "fechaInicio", source = "periodo.fechaInicio")
    @Mapping(target = "fechaVencimiento", source = "periodo.fechaVencimiento")
    @Mapping(target = "fechaDevolucionReal", source = "periodo.fechaDevolucionReal")
    @Mapping(target = "lugarUso", source = "lugarUso")
    @Mapping(target = "estado", source = "estado")
    PrestamoEntidad aEntidad(Prestamo dominio);

    @Mapping(target = "idReserva", source = "idReserva")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "ejemplar", source = "ejemplar")
    @Mapping(target = "fechaReserva", source = "fechaReserva")
    @Mapping(target = "fechaExpiracion", source = "fechaExpiracion")
    @Mapping(target = "estado", source = "estado")
    Reserva aDominio(ReservaEntidad entidad);

    @Mapping(target = "idReserva", source = "idReserva")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "ejemplar", source = "ejemplar")
    @Mapping(target = "fechaReserva", source = "fechaReserva")
    @Mapping(target = "fechaExpiracion", source = "fechaExpiracion")
    @Mapping(target = "estado", source = "estado")
    ReservaEntidad aEntidad(Reserva dominio);

    List<Prestamo> aDominioLista(List<PrestamoEntidad> entidades);
    List<Reserva> aDominioLista(List<ReservaEntidad> entidades);
}