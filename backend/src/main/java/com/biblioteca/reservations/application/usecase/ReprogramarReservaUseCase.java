package com.biblioteca.reservations.application.usecase;

import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.repository.ReservaRepository;
import com.biblioteca.reservations.domain.vo.ReservaId;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.NotFoundException;
import com.biblioteca.inventory.domain.repository.EjemplarRepository;
import com.biblioteca.reservations.application.port.DisponibilidadProgramadaPort;
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
    private final DisponibilidadProgramadaPort disponibilidad;
    private final com.biblioteca.identity.domain.repository.UsuarioRepository usuarios;
    private final com.biblioteca.catalog.domain.repository.CatalogoItemRepository catalogo;
    private final com.biblioteca.circulation.domain.policy.PoliticaPrestamo politica;

    public ReprogramarReservaUseCase(ReservaRepository reservas, Clock clock, ZoneId zona,
                                     EjemplarRepository ejemplares,
                                     DisponibilidadProgramadaPort disponibilidad,
                                     com.biblioteca.identity.domain.repository.UsuarioRepository usuarios,
                                     com.biblioteca.catalog.domain.repository.CatalogoItemRepository catalogo,
                                     com.biblioteca.circulation.domain.policy.PoliticaPrestamo politica) {
        this.reservas = reservas;
        this.clock = clock;
        this.zona = zona;
        this.ejemplares = ejemplares;
        this.disponibilidad = disponibilidad;
        this.usuarios = usuarios;
        this.catalogo = catalogo;
        this.politica = politica;
    }

    public Reserva ejecutar(UsuarioId usuarioId, ReservaId reservaId, Instant inicio, Instant fin) {
        Reserva reserva = reservas.buscarPorIdParaActualizar(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva", reservaId.value()));
        if (!reserva.prestatarioId().equals(usuarioId)) {
            throw new BusinessRuleException("REPROGRAMACION_NO_PERMITIDA",
                    "Solo el titular puede reprogramar su reserva");
        }
        var usuario = usuarios.buscarPorIdParaActualizar(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario", usuarioId.value()));
        if (!usuario.puedeSolicitarPrestamo())
            throw new BusinessRuleException("USUARIO_NO_HABILITADO", "La cuenta no puede reprogramar reservas");
        var item = catalogo.buscarPorId(reserva.catalogoItemId())
                .orElseThrow(() -> new NotFoundException("Item", reserva.catalogoItemId().value()));
        com.biblioteca.circulation.domain.policy.PoliticaPlazoPrestamo.validar(
                inicio, fin, reserva.lugarUso(), usuario.perfil(), item.tipo(), politica);
        if (ejemplares.listarPorItem(reserva.catalogoItemId()).stream().noneMatch(e -> e.puedeProgramarse()
                && disponibilidad.estaLibre(e.id(), inicio, fin, reserva.id()))) {
            throw new BusinessRuleException("EJEMPLAR_NO_DISPONIBLE",
                    "No hay ejemplares libres durante el nuevo horario");
        }
        reserva.reprogramar(inicio, fin, clock.instant(), zona);
        reservas.guardar(reserva);
        return reserva;
    }
}
