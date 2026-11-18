package com.biblioteca.circulation.domain.policy;

import com.biblioteca.catalog.domain.model.TipoItem;
import com.biblioteca.circulation.domain.model.LugarUso;
import com.biblioteca.identity.domain.model.PerfilPrestatario;
import com.biblioteca.shared.domain.BusinessRuleException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

/** Plazos compartidos por reservas, entregas y renovaciones. Un dia equivale a 24 horas. */
public final class PoliticaPlazoPrestamo {
    public static final ZoneId ZONA = ZoneId.of("America/Lima");
    public static final LocalTime APERTURA = LocalTime.of(7, 0);
    public static final LocalTime CIERRE = LocalTime.of(20, 0);
    public static final Duration MINIMO = Duration.ofHours(1);
    public static final Duration MAXIMO_CAMPUS = Duration.ofHours(13);

    private PoliticaPlazoPrestamo() { }

    public static void validar(Instant inicio, Instant fin, LugarUso lugar,
                               PerfilPrestatario perfil, TipoItem tipo, PoliticaPrestamo politica) {
        Objects.requireNonNull(lugar, "El lugar de uso es obligatorio");
        if (lugar == LugarUso.DOMICILIO && !politica.permiteDomicilio(perfil, tipo)) {
            rechazar("DOMICILIO_NO_PERMITIDO", "Este bien solo se permite en campus para este perfil");
        }
        Duration maximo = lugar == LugarUso.EN_CAMPUS ? MAXIMO_CAMPUS
                : Duration.ofDays(politica.diasPrestamo(perfil, tipo));
        validarHorario(inicio, fin, lugar, maximo);
    }

    public static void validarHorario(Instant inicio, Instant fin, LugarUso lugar, Duration maximo) {
        Objects.requireNonNull(inicio, "La fecha y hora inicial son obligatorias");
        Objects.requireNonNull(fin, "La fecha y hora final son obligatorias");
        Objects.requireNonNull(lugar, "El lugar de uso es obligatorio");
        Duration duracion = Duration.between(inicio, fin);
        if (duracion.compareTo(MINIMO) < 0) {
            rechazar("DURACION_PRESTAMO_INSUFICIENTE", "El plazo debe durar al menos una hora");
        }
        if (duracion.compareTo(maximo) > 0) {
            rechazar("PLAZO_PRESTAMO_EXCEDIDO", "El plazo supera el maximo permitido: "
                    + maximo.toHours() + " horas");
        }
        var entrega = inicio.atZone(ZONA);
        var devolucion = fin.atZone(ZONA);
        if (lugar == LugarUso.EN_CAMPUS && !entrega.toLocalDate().equals(devolucion.toLocalDate())) {
            rechazar("FUERA_DE_HORARIO", "En campus la entrega y devolucion deben ser el mismo dia");
        }
        if (entrega.toLocalTime().isBefore(APERTURA) || entrega.toLocalTime().isAfter(CIERRE)
                || devolucion.toLocalTime().isBefore(APERTURA) || devolucion.toLocalTime().isAfter(CIERRE)) {
            rechazar("FUERA_DE_HORARIO", "La entrega y devolucion se atienden entre 07:00 y 20:00 (Lima)");
        }
    }

    public static Instant vencimientoPredeterminado(Instant entrega, LugarUso lugar,
                                                    PerfilPrestatario perfil, TipoItem tipo,
                                                    PoliticaPrestamo politica) {
        return lugar == LugarUso.EN_CAMPUS
                ? entrega.atZone(ZONA).toLocalDate().atTime(CIERRE).atZone(ZONA).toInstant()
                : entrega.plus(Duration.ofDays(politica.diasPrestamo(perfil, tipo)));
    }

    private static void rechazar(String codigo, String mensaje) {
        throw new BusinessRuleException(codigo, mensaje);
    }
}
