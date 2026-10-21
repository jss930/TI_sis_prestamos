package com.biblioteca.support;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.model.EquipoDetalle;
import com.biblioteca.catalog.domain.model.LibroDetalle;
import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.catalog.domain.vo.Autor;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.catalog.domain.vo.ISBN;
import com.biblioteca.catalog.domain.vo.Titulo;
import com.biblioteca.identity.domain.model.EstadoCuentaUsuario;
import com.biblioteca.identity.domain.model.PerfilPrestatario;
import com.biblioteca.identity.domain.model.PermisoOperativo;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.NombreCompleto;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.model.Ejemplar;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.inventory.domain.model.Ubicacion;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.inventory.domain.vo.EjemplarId;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.Clock;
import java.util.EnumSet;
import java.util.UUID;

public final class Fixtures {

    public static final Instant AHORA = Instant.parse("2026-10-20T12:00:00Z");
    public static final LocalDate HOY = LocalDate.of(2026, 10, 20);
    public static final ZoneOffset ZONA = ZoneOffset.UTC;

    private Fixtures() {
    }

    public static Clock clock() {
        return Clock.fixed(AHORA, ZONA);
    }

    public static Clock clockEn(LocalDate fecha) {
        return Clock.fixed(fecha.atStartOfDay().toInstant(ZONA), ZONA);
    }

    public static Usuario bibliotecario() {
        return Usuario.reconstruir(
                new UsuarioId(UUID.randomUUID()),
                new EmailAddress("bibliotecario@biblioteca.pe"),
                new NombreCompleto("Elena Bibliotecaria"),
                PerfilPrestatario.ADMINISTRATIVO,
                EstadoCuentaUsuario.VALIDADO,
                EnumSet.allOf(PermisoOperativo.class),
                AHORA.minusSeconds(10_000),
                AHORA.minusSeconds(5_000),
                null);
    }

    public static Usuario alumnoValidado() {
        return Usuario.reconstruir(
                new UsuarioId(UUID.randomUUID()),
                new EmailAddress("alumno-" + UUID.randomUUID() + "@universidad.edu.pe"),
                new NombreCompleto("Luis Quispe"),
                PerfilPrestatario.ALUMNO,
                EstadoCuentaUsuario.VALIDADO,
                EnumSet.noneOf(PermisoOperativo.class),
                AHORA.minusSeconds(10_000),
                AHORA.minusSeconds(5_000),
                null);
    }

    public static Usuario alumnoProvisional() {
        return Usuario.reconstruir(
                new UsuarioId(UUID.randomUUID()),
                new EmailAddress("provisional-" + UUID.randomUUID() + "@universidad.edu.pe"),
                new NombreCompleto("Ana Torres"),
                PerfilPrestatario.ALUMNO,
                EstadoCuentaUsuario.PROVISIONAL,
                EnumSet.noneOf(PermisoOperativo.class),
                AHORA.minusSeconds(5_000),
                null,
                null);
    }

    public static Usuario provisionalConPermisoDeOperar() {
        return Usuario.reconstruir(
                new UsuarioId(UUID.randomUUID()),
                new EmailAddress("provisional-permiso-" + UUID.randomUUID() + "@universidad.edu.pe"),
                new NombreCompleto("Ana con permiso"),
                PerfilPrestatario.ALUMNO,
                EstadoCuentaUsuario.PROVISIONAL,
                EnumSet.of(PermisoOperativo.REGISTRAR_PRESTAMO),
                AHORA.minusSeconds(5_000),
                null,
                null);
    }

    public static Usuario docenteValidado() {
        return Usuario.reconstruir(
                new UsuarioId(UUID.randomUUID()),
                new EmailAddress("docente-" + UUID.randomUUID() + "@universidad.edu.pe"),
                new NombreCompleto("Rosa Docente"),
                PerfilPrestatario.DOCENTE,
                EstadoCuentaUsuario.VALIDADO,
                EnumSet.noneOf(PermisoOperativo.class),
                AHORA.minusSeconds(10_000),
                AHORA.minusSeconds(5_000),
                null);
    }

    public static CatalogoItem libro() {
        return CatalogoItem.reconstruir(
                new CatalogoItemId(UUID.randomUUID()),
                new Titulo("Clean Code"),
                TipoItem.LIBRO,
                new LibroDetalle(new ISBN("9780132350884"), new Autor("Robert C. Martin"),
                        "Prentice Hall", 2008),
                true,
                AHORA.minusSeconds(10_000));
    }

    public static CatalogoItem laptop() {
        return CatalogoItem.reconstruir(
                new CatalogoItemId(UUID.randomUUID()),
                new Titulo("Laptop Lenovo ThinkPad T14"),
                TipoItem.EQUIPO,
                new EquipoDetalle("Lenovo", "ThinkPad T14"),
                true,
                AHORA.minusSeconds(10_000));
    }

    public static Ejemplar ejemplarDisponible(CatalogoItemId itemId) {
        return Ejemplar.reconstruir(
                new EjemplarId(UUID.randomUUID()),
                itemId,
                barcodeUnico(),
                new Ubicacion("Sala general", "Estante A-1"),
                EstadoFisico.DISPONIBLE,
                null,
                AHORA.minusSeconds(10_000));
    }

    public static Ejemplar ejemplarPrestado(CatalogoItemId itemId) {
        return Ejemplar.reconstruir(
                new EjemplarId(UUID.randomUUID()),
                itemId,
                barcodeUnico(),
                new Ubicacion("Sala general", "Estante A-1"),
                EstadoFisico.PRESTADO,
                null,
                AHORA.minusSeconds(10_000));
    }

    public static Barcode barcodeUnico() {
        return new Barcode("LIB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}
