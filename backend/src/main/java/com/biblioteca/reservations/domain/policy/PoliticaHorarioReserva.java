package com.biblioteca.reservations.domain.policy;

import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.circulation.domain.model.LugarUso;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

/** Reglas únicas del horario de una reserva futura. */
public final class PoliticaHorarioReserva {
    public static final Duration ANTICIPACION_MINIMA = Duration.ofMinutes(30);
    public static final Duration DURACION_MINIMA = Duration.ofHours(1);
    public static final Duration DURACION_MAXIMA = Duration.ofHours(24);
    public static final Duration TOLERANCIA_RECOJO = Duration.ofHours(1);
    public static final Duration LIMITE_REPROGRAMACION = Duration.ofHours(1);
    public static final LocalTime APERTURA = LocalTime.of(7, 0);
    public static final LocalTime CIERRE = LocalTime.of(20, 0);

    private PoliticaHorarioReserva() { }

    public static void validar(Instant inicio, Instant fin, Instant ahora, ZoneId zona, LugarUso lugarUso) {
        Objects.requireNonNull(inicio, "La fecha y hora inicial son obligatorias");
        Objects.requireNonNull(fin, "La fecha y hora final son obligatorias");
        Objects.requireNonNull(ahora, "La hora actual es obligatoria");
        Objects.requireNonNull(zona, "La zona horaria es obligatoria");
        Objects.requireNonNull(lugarUso, "El lugar de uso es obligatorio");
        if (inicio.isBefore(ahora.plus(ANTICIPACION_MINIMA))) {
            rechazar("ANTICIPACION_INSUFICIENTE", "Reserva con al menos 30 minutos de anticipacion");
        }
        if (!fin.isAfter(inicio)) {
            rechazar("HORARIO_INVALIDO", "La hora final debe ser posterior a la inicial");
        }
        Duration duracion = Duration.between(inicio, fin);
        if (duracion.compareTo(DURACION_MINIMA) < 0) {
            rechazar("DURACION_RESERVA_INSUFICIENTE", "La reserva debe durar al menos una hora");
        }
        if (duracion.compareTo(DURACION_MAXIMA) > 0) {
            rechazar("DURACION_RESERVA_EXCEDIDA", "La reserva no puede durar mas de 24 horas");
        }
        var inicioLocal = inicio.atZone(zona);
        var finLocal = fin.atZone(zona);
        if (lugarUso == LugarUso.EN_CAMPUS
                && !LocalDate.from(inicioLocal).equals(LocalDate.from(finLocal))) {
            rechazar("FUERA_DE_HORARIO", "Las reservas en campus deben iniciar y terminar el mismo dia");
        }
        if (inicioLocal.toLocalTime().isBefore(APERTURA)
                || inicioLocal.toLocalTime().isAfter(CIERRE)
                || finLocal.toLocalTime().isBefore(APERTURA)
                || finLocal.toLocalTime().isAfter(CIERRE)) {
            rechazar("FUERA_DE_HORARIO", "Las reservas se atienden entre 07:00 y 20:00");
        }
    }

    public static boolean seSolapan(Instant inicioA, Instant finA, Instant inicioB, Instant finB) {
        return inicioA.isBefore(finB) && inicioB.isBefore(finA);
    }

    private static void rechazar(String codigo, String mensaje) {
        throw new BusinessRuleException(codigo, mensaje);
    }
}
