package pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador;

import pe.edu.unsa.sisprestamos.dominio.politicas_sanciones.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PoliticasSancionesMapeador {

    PoliticasSancionesMapeador INSTANCE = Mappers.getMapper(PoliticasSancionesMapeador.class);

    @Mapping(target = "idSancion", source = "idSancion")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "prestamo", source = "prestamo")
    @Mapping(target = "motivo", source = "motivo")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    @Mapping(target = "estado", source = "estado")
    Sancion aDominio(SancionEntidad entidad);

    @Mapping(target = "idSancion", source = "idSancion")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "prestamo", source = "prestamo")
    @Mapping(target = "motivo", source = "motivo")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    @Mapping(target = "estado", source = "estado")
    SancionEntidad aEntidad(Sancion dominio);

    @Mapping(target = "idPolitica", source = "idPolitica")
    @Mapping(target = "tipoPersona", source = "tipoPersona")
    @Mapping(target = "tipoItem", source = "tipoItem")
    @Mapping(target = "duracionMaximaDias", source = "duracionMaximaDias")
    @Mapping(target = "cantidadMaximaSimultanea", source = "cantidadMaximaSimultanea")
    @Mapping(target = "permitePrestamoDomicilio", source = "permitePrestamoDomicilio")
    PoliticaPrestamo aDominio(PoliticaPrestamoEntidad entidad);

    @Mapping(target = "idPolitica", source = "idPolitica")
    @Mapping(target = "tipoPersona", source = "tipoPersona")
    @Mapping(target = "tipoItem", source = "tipoItem")
    @Mapping(target = "duracionMaximaDias", source = "duracionMaximaDias")
    @Mapping(target = "cantidadMaximaSimultanea", source = "cantidadMaximaSimultanea")
    @Mapping(target = "permitePrestamoDomicilio", source = "permitePrestamoDomicilio")
    PoliticaPrestamoEntidad aEntidad(PoliticaPrestamo dominio);

    @Mapping(target = "idHistorial", source = "idHistorial")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "prestamo", source = "prestamo")
    @Mapping(target = "tipoEvento", source = "tipoEvento")
    @Mapping(target = "fechaEvento", source = "fechaEvento")
    HistorialPrestamo aDominio(HistorialPrestamoEntidad entidad);

    @Mapping(target = "idHistorial", source = "idHistorial")
    @Mapping(target = "persona", source = "persona")
    @Mapping(target = "prestamo", source = "prestamo")
    @Mapping(target = "tipoEvento", source = "tipoEvento")
    @Mapping(target = "fechaEvento", source = "fechaEvento")
    HistorialPrestamoEntidad aEntidad(HistorialPrestamo dominio);

    List<Sancion> aDominioLista(List<SancionEntidad> entidades);
    List<PoliticaPrestamo> aDominioLista(List<PoliticaPrestamoEntidad> entidades);
    List<HistorialPrestamo> aDominioLista(List<HistorialPrestamoEntidad> entidades);
}