package com.biblioteca.infrastructure.web.rest;

import com.biblioteca.circulation.application.usecase.InspeccionarDevolucionUseCase;
import com.biblioteca.circulation.domain.model.InspeccionDevolucion;
import com.biblioteca.circulation.domain.vo.PrestamoId;
import com.biblioteca.inventory.domain.model.EstadoFisico;
import com.biblioteca.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class InspeccionDevolucionController {
    private final InspeccionarDevolucionUseCase inspeccionar;
    private final UsuarioAutenticado autenticado;
    public InspeccionDevolucionController(InspeccionarDevolucionUseCase inspeccionar, UsuarioAutenticado autenticado) {
        this.inspeccionar = inspeccionar;
        this.autenticado = autenticado;
    }
    @GetMapping("/inspecciones-devolucion")
    public List<InspeccionResponse> listar(@RequestHeader(value = "Authorization", required = false) String token) {
        return inspeccionar.listar(autenticado.exigir(token)).stream().map(this::respuesta).toList();
    }
    @PostMapping("/prestamos/{id}/inspeccion-devolucion")
    public InspeccionResponse registrar(@PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody InspeccionRequest request) {
        return respuesta(inspeccionar.registrar(autenticado.exigir(token), new PrestamoId(id),
                request.estadoFisico(), request.accesoriosFaltantes(), request.observaciones()));
    }
    private InspeccionResponse respuesta(InspeccionDevolucion i) {
        return new InspeccionResponse(i.prestamoId(), i.recibidoPorId(), i.estadoFisico(),
                i.accesoriosFaltantes(), i.observaciones(), i.fecha(), i.estadoResultante());
    }
    public record InspeccionRequest(@NotNull EstadoFisico estadoFisico,
            @NotNull Boolean accesoriosFaltantes, @Size(max = 1000) String observaciones) { }
    public record InspeccionResponse(UUID prestamoId, UUID recibidoPorId, EstadoFisico estadoFisico,
            boolean accesoriosFaltantes, String observaciones, Instant fecha, EstadoFisico estadoResultante) { }
}
