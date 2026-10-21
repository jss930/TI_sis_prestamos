package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.identity.domain.model.PermisoOperativo;
import com.biblioteca.identity.domain.model.Usuario;
import com.biblioteca.identity.domain.repository.UsuarioRepository;
import com.biblioteca.identity.domain.vo.EmailAddress;
import com.biblioteca.identity.domain.vo.NombreCompleto;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.infrastructure.persistence.jpa.entity.UsuarioJpaEntity;
import com.biblioteca.infrastructure.persistence.jpa.repository.UsuarioJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Adaptador JPA del puerto de identidad. */
@Repository
@ConditionalOnProperty(name = "biblioteca.persistence", havingValue = "jpa", matchIfMissing = true)
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository repositorio;

    public UsuarioJpaAdapter(UsuarioJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        repositorio.save(aEntidad(usuario));
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(UsuarioId id) {
        return repositorio.findById(id.value()).map(UsuarioJpaAdapter::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(EmailAddress email) {
        return repositorio.findByEmail(email.value()).map(UsuarioJpaAdapter::aDominio);
    }

    @Override
    public List<Usuario> listarTodos() {
        return repositorio.findAll().stream().map(UsuarioJpaAdapter::aDominio).toList();
    }

    @Override
    public boolean existePorEmail(EmailAddress email) {
        return repositorio.existsByEmail(email.value());
    }

    private static UsuarioJpaEntity aEntidad(Usuario usuario) {
        UsuarioJpaEntity entidad = new UsuarioJpaEntity();
        entidad.id = usuario.id().value();
        entidad.email = usuario.email().value();
        entidad.nombreCompleto = usuario.nombreCompleto().value();
        entidad.perfil = usuario.perfil();
        entidad.estado = usuario.estado();
        entidad.permisos = usuario.permisos().stream().map(Enum::name).sorted()
                .collect(Collectors.joining(","));
        entidad.fechaAlta = usuario.fechaAlta();
        entidad.fechaValidacion = usuario.fechaValidacion();
        entidad.motivoSuspension = usuario.motivoSuspension();
        return entidad;
    }

    private static Usuario aDominio(UsuarioJpaEntity entidad) {
        return Usuario.reconstruir(new UsuarioId(entidad.id), new EmailAddress(entidad.email),
                new NombreCompleto(entidad.nombreCompleto), entidad.perfil, entidad.estado,
                permisosDe(entidad.permisos), entidad.fechaAlta, entidad.fechaValidacion,
                entidad.motivoSuspension);
    }

    private static Set<PermisoOperativo> permisosDe(String valor) {
        if (valor == null || valor.isBlank()) {
            return EnumSet.noneOf(PermisoOperativo.class);
        }
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .filter(permiso -> !permiso.isEmpty())
                .map(PermisoOperativo::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PermisoOperativo.class)));
    }
}
