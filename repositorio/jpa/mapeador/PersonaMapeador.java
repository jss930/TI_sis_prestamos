package pe.edu.unsa.sisprestamos.repositorio.jpa.mapeador;

import pe.edu.unsa.sisprestamos.dominio.usuarios.*;
import pe.edu.unsa.sisprestamos.repositorio.jpa.entidad.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonaMapeador {

    PersonaMapeador INSTANCE = Mappers.getMapper(PersonaMapeador.class);

    @Mapping(target = "idPersona", source = "idPersona")
    @Mapping(target = "contacto.correo", source = "contactoCorreo")
    @Mapping(target = "contacto.telefono", source = "contactoTelefono")
    @Mapping(target = "estado", source = "estado")
    Persona aDominio(PersonaEntidad entidad);

    @Mapping(target = "idPersona", source = "idPersona")
    @Mapping(target = "contactoCorreo", source = "contacto.correo")
    @Mapping(target = "contactoTelefono", source = "contacto.telefono")
    @Mapping(target = "estado", source = "estado")
    PersonaEntidad aEntidad(Persona dominio);

    @Mapping(target = "idAdmin", source = "idAdmin")
    @Mapping(target = "permisos", source = "permisos")
    Admin aDominio(AdminEntidad entidad);

    @Mapping(target = "idAdmin", source = "idAdmin")
    @Mapping(target = "permisos", source = "permisos")
    AdminEntidad aEntidad(Admin dominio);

    @Mapping(target = "idPerfil", source = "idPerfil")
    @Mapping(target = "prestamoDomicilioEquipos", source = "prestamoDomicilioEquipos")
    @Mapping(target = "plazoExtendidoLibros", source = "plazoExtendidoLibros")
    @Mapping(target = "cantidadMaximaSimultanea", source = "cantidadMaximaSimultanea")
    Perfil aDominio(PerfilEntidad entidad);

    @Mapping(target = "idPerfil", source = "idPerfil")
    @Mapping(target = "prestamoDomicilioEquipos", source = "prestamoDomicilioEquipos")
    @Mapping(target = "plazoExtendidoLibros", source = "plazoExtendidoLibros")
    @Mapping(target = "cantidadMaximaSimultanea", source = "cantidadMaximaSimultanea")
    PerfilEntidad aEntidad(Perfil dominio);

    @Mapping(target = "idAlumno", source = "idAlumno")
    @Mapping(target = "cui", source = "cui")
    Alumno aDominio(AlumnoEntidad entidad);

    @Mapping(target = "idAlumno", source = "idAlumno")
    @Mapping(target = "cui", source = "cui")
    AlumnoEntidad aEntidad(Alumno dominio);

    @Mapping(target = "idDocente", source = "idDocente")
    @Mapping(target = "departamento", source = "departamento")
    @Mapping(target = "categoria", source = "categoria")
    Docente aDominio(DocenteEntidad entidad);

    @Mapping(target = "idDocente", source = "idDocente")
    @Mapping(target = "departamento", source = "departamento")
    @Mapping(target = "categoria", source = "categoria")
    DocenteEntidad aEntidad(Docente dominio);

    @Mapping(target = "idAdministrativo", source = "idAdministrativo")
    @Mapping(target = "area", source = "area")
    Administrativo aDominio(AdministrativoEntidad entidad);

    @Mapping(target = "idAdministrativo", source = "idAdministrativo")
    @Mapping(target = "area", source = "area")
    AdministrativoEntidad aEntidad(Administrativo dominio);
}