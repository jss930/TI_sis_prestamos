package com.biblioteca.reservations.application.usecase;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Service
@Transactional
public class ReprogramarReservaUseCase {
    private final ReservaRepository reservas;
    private final Clock clock;
    private final ZoneId zona;
    private final EjemplarRepository ejemplares;

    public ReprogramarReservaUseCase(ReservaRepository reservas, Clock clock, ZoneId zona,
                                     EjemplarRepository ejemplares) {
        this.reservas = reservas;
        this.clock = clock;
        this.zona = zona;
        this.ejemplares = ejemplares;
    }

    public Reserva ejecutar(UsuarioId usuarioId, ReservaId reservaId, Instant inicio, Instant fin) {
        Reserva reserva = reservas.buscarPorIdParaActualizar(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva", reservaId.value()));
        if (!reserva.prestatarioId().equals(usuarioId)) {
            throw new BusinessRuleException("REPROGRAMACION_NO_PERMITIDA",
                    "Solo el titular puede reprogramar su reserva");
        }
        if (ejemplares.listarPorItem(reserva.catalogoItemId()).stream().noneMatch(e -> e.estaDisponible()
                && !reservas.existeSolapamiento(e.id(), inicio, fin, reserva.id()))) {
            throw new BusinessRuleException("EJEMPLAR_NO_DISPONIBLE",
                    "No hay ejemplares libres durante el nuevo horario");
        }
        reserva.reprogramar(inicio, fin, clock.instant(), zona);
        reservas.guardar(reserva);
        return reserva;
    }
}
