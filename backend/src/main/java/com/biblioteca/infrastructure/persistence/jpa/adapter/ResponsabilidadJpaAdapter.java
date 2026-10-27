package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.identity.domain.model.*;
import com.biblioteca.identity.domain.repository.ResponsabilidadRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.jpa.entity.*;
import com.biblioteca.infrastructure.persistence.jpa.repository.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "jpa", matchIfMissing = true)
public class ResponsabilidadJpaAdapter implements ResponsabilidadRepository {
    private final ResponsabilidadUsuarioJpaRepository responsabilidades;
    private final CambioResponsableJpaRepository cambios;
    private final ControlResponsablesJpaRepository control;

    public ResponsabilidadJpaAdapter(ResponsabilidadUsuarioJpaRepository responsabilidades,
                                     CambioResponsableJpaRepository cambios, ControlResponsablesJpaRepository control) {
        this.responsabilidades = responsabilidades;
        this.cambios = cambios;
        this.control = control;
    }

    public void bloquearAsignaciones() { control.bloquear(); }

    public Optional<ResponsabilidadUsuario> buscar(UsuarioId id) {
        return responsabilidades.findById(id.value()).map(this::aDominio);
    }
    public List<ResponsabilidadUsuario> listar() {
        return responsabilidades.findAll().stream().map(this::aDominio).toList();
    }
    public void guardar(ResponsabilidadUsuario responsabilidad) {
        var entidad = new ResponsabilidadUsuarioJpaEntity();
        entidad.usuarioId = responsabilidad.usuarioId().value();
        entidad.capacitacionVerificada = responsabilidad.capacitacionVerificada();
        entidad.categorias = responsabilidad.categorias().stream().map(Enum::name).sorted().collect(Collectors.joining(","));
        entidad.cargos.putAll(responsabilidad.cargos());
        responsabilidades.save(entidad);
    }
    public void registrarCambio(CambioResponsable cambio) {
        var entidad = new CambioResponsableJpaEntity();
        entidad.id = cambio.id();
        entidad.usuarioId = cambio.usuarioId();
        entidad.operadorId = cambio.operadorId();
        entidad.categoria = cambio.categoria();
        entidad.accion = cambio.accion();
        entidad.cargo = cambio.cargo();
        entidad.fecha = cambio.fecha();
        cambios.save(entidad);
    }
    public List<CambioResponsable> historial() {
        return cambios.findAllByOrderByFechaDesc().stream().map(e -> new CambioResponsable(
                e.id, e.usuarioId, e.operadorId, e.categoria, e.accion, e.fecha, e.cargo)).toList();
    }
    private ResponsabilidadUsuario aDominio(ResponsabilidadUsuarioJpaEntity e) {
        return ResponsabilidadUsuario.reconstruir(new UsuarioId(e.usuarioId), e.capacitacionVerificada, e.cargos);
    }
}
