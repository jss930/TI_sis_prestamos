package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.circulation.application.usecase.ConsultarHistorialUseCase;
import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.security.AccesoHistorial;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reportes")
public class ReporteController {
    private final AccesoHistorial acceso;
    private final ConsultarHistorialUseCase consultar;

    public ReporteController(AccesoHistorial acceso, ConsultarHistorialUseCase consultar) {
        this.acceso = acceso;
        this.consultar = consultar;
    }

    @GetMapping("/usuarios/{id}")
    public ConsultarHistorialUseCase.Historial usuario(@PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) EstadoPrestamo estado) {
        var actor = acceso.actor(token);
        var usuarioId = new UsuarioId(id);
        acceso.exigirReporteUsuario(actor, usuarioId);
        return consultar.porUsuario(usuarioId, desde, hasta, estado);
    }

    @GetMapping("/ejemplares/{id}")
    public ConsultarHistorialUseCase.Historial ejemplar(@PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) EstadoPrestamo estado) {
        acceso.exigirAdministrador(acceso.actor(token));
        return consultar.porEjemplar(new EjemplarId(id), desde, hasta, estado);
    }
}
