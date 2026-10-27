package com.biblioteca.administration.application.usecase;

import com.biblioteca.catalog.domain.model.*;
import com.biblioteca.catalog.domain.repository.CatalogoItemRepository;
import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.shared.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AsignarAreaCatalogoUseCase {
    private final CatalogoItemRepository catalogo;
    private final UsuarioRepository usuarios;
    public AsignarAreaCatalogoUseCase(CatalogoItemRepository catalogo, UsuarioRepository usuarios) {
        this.catalogo = catalogo; this.usuarios = usuarios;
    }
    public CatalogoItem ejecutar(UsuarioId operadorId, CatalogoItemId itemId, AreaCatalogo area) {
        var operador = usuarios.buscarPorId(operadorId).orElseThrow(() -> new NotFoundException("Usuario", operadorId.value()));
        if (!operador.puedeAdministrarSistema()) throw new BusinessRuleException("OPERADOR_NO_HABILITADO", "Solo la administracion puede cambiar el area del bien");
        var item = catalogo.buscarPorId(itemId).orElseThrow(() -> new NotFoundException("Item", itemId.value()));
        item.asignarArea(area);
        return catalogo.guardar(item);
    }
}
