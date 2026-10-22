package com.biblioteca.identity.domain.repository;

import com.biblioteca.identity.domain.model.ResponsabilidadUsuario;
import com.biblioteca.identity.domain.model.CambioResponsable;
import com.biblioteca.identity.domain.vo.UsuarioId;
import java.util.List;
import java.util.Optional;

public interface ResponsabilidadRepository {
    Optional<ResponsabilidadUsuario> buscar(UsuarioId id);
    List<ResponsabilidadUsuario> listar();
    void guardar(ResponsabilidadUsuario responsabilidad);
    void registrarCambio(CambioResponsable cambio);
    List<CambioResponsable> historial();
}
