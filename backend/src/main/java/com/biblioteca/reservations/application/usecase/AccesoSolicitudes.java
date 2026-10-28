package com.biblioteca.reservations.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.shared.domain.*;
import org.springframework.stereotype.Service;

@Service
public class AccesoSolicitudes {
    private final UsuarioRepository usuarios;
    private final CatalogoItemRepository catalogo;
    public AccesoSolicitudes(UsuarioRepository usuarios, CatalogoItemRepository catalogo) {
        this.usuarios = usuarios; this.catalogo = catalogo;
    }
    public Usuario usuario(UsuarioId id) { return usuarios.buscarPorId(id).orElseThrow(() -> new NotFoundException("Usuario", id.value())); }
    public CatalogoItem item(Reserva reserva) { return catalogo.buscarPorId(reserva.catalogoItemId()).orElseThrow(() -> new NotFoundException("Item", reserva.catalogoItemId().value())); }
    public boolean puedeAtender(UsuarioId id, Reserva reserva) {
        return usuario(id).puedeAdministrarSistema();
    }
    public void exigirAdministrador(UsuarioId id, Reserva reserva) {
        if (!puedeAtender(id, reserva)) {
            throw new BusinessRuleException("ADMINISTRADOR_REQUERIDO",
                    "Solo un administrador puede aprobar, denegar o entregar reservas");
        }
    }
    public void exigirLectura(UsuarioId id, Reserva reserva) {
        if (!reserva.prestatarioId().equals(id)) exigirAdministrador(id, reserva);
    }
}
