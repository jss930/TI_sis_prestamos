package com.biblioteca.reservations.application.usecase;

import com.biblioteca.catalog.domain.model.CatalogoItem;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.identity.domain.repository.*;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.reservations.domain.model.Reserva;
import com.biblioteca.reservations.domain.policy.PoliticaAtencionSolicitud;
import com.biblioteca.shared.domain.*;
import org.springframework.stereotype.Service;

@Service
public class AccesoSolicitudes {
    private final UsuarioRepository usuarios;
    private final ResponsabilidadRepository responsabilidades;
    private final CatalogoItemRepository catalogo;
    public AccesoSolicitudes(UsuarioRepository usuarios, ResponsabilidadRepository responsabilidades, CatalogoItemRepository catalogo) {
        this.usuarios = usuarios; this.responsabilidades = responsabilidades; this.catalogo = catalogo;
    }
    public Usuario usuario(UsuarioId id) { return usuarios.buscarPorId(id).orElseThrow(() -> new NotFoundException("Usuario", id.value())); }
    public CatalogoItem item(Reserva reserva) { return catalogo.buscarPorId(reserva.catalogoItemId()).orElseThrow(() -> new NotFoundException("Item", reserva.catalogoItemId().value())); }
    public boolean puedeAtender(UsuarioId id, Reserva reserva) {
        return PoliticaAtencionSolicitud.puedeAtender(usuario(id), responsabilidades.buscar(id).orElse(null), item(reserva).area());
    }
    public void exigirResponsable(UsuarioId id, Reserva reserva) {
        PoliticaAtencionSolicitud.exigirResponsable(usuario(id), responsabilidades.buscar(id).orElse(null), item(reserva).area());
    }
    public void exigirLectura(UsuarioId id, Reserva reserva) {
        if (!reserva.prestatarioId().equals(id)) exigirResponsable(id, reserva);
    }
}
