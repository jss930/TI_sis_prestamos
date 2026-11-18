package com.biblioteca.infrastructure.persistence.jpa.adapter;

import com.biblioteca.circulation.application.port.HistorialPrestamosPort;
import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.infrastructure.persistence.jpa.entity.*;
import com.biblioteca.infrastructure.persistence.jpa.repository.PrestamoJpaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@Repository
public class HistorialPrestamosJpaAdapter implements HistorialPrestamosPort {
    private final PrestamoJpaRepository repositorio;
    public HistorialPrestamosJpaAdapter(PrestamoJpaRepository repositorio) { this.repositorio = repositorio; }

    @Override
    public Resultado consultar(Filtro filtro) {
        Specification<PrestamoJpaEntity> spec = (p, consulta, cb) -> {
            var condiciones = new ArrayList<Predicate>();
            if (filtro.usuario() != null) condiciones.add(cb.equal(p.get("prestatarioId"), filtro.usuario().value()));
            if (filtro.ejemplar() != null) condiciones.add(cb.equal(p.get("ejemplarId"), filtro.ejemplar().value()));
            if (filtro.desde() != null) condiciones.add(cb.greaterThanOrEqualTo(p.get("fechaHoraEntrega"), filtro.desde()));
            if (filtro.hastaExclusivo() != null) condiciones.add(cb.lessThan(p.get("fechaHoraEntrega"), filtro.hastaExclusivo()));
            if (filtro.estado() != null) condiciones.add(cb.equal(p.get("estado"), filtro.estado()));
            if (filtro.busqueda() != null && !filtro.busqueda().isBlank()) {
                String texto = "%" + filtro.busqueda().trim().toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                var personas = consulta.subquery(UUID.class);
                var u = personas.from(UsuarioJpaEntity.class);
                personas.select(u.get("id")).where(cb.or(cb.like(cb.lower(u.get("nombreCompleto")), texto, '\\'),
                        cb.like(cb.lower(u.get("email")), texto, '\\'),
                        cb.like(cb.lower(u.get("codigoInstitucional")), texto, '\\')));
                var copias = consulta.subquery(UUID.class);
                var e = copias.from(EjemplarJpaEntity.class);
                var item = copias.from(CatalogoItemJpaEntity.class);
                copias.select(e.get("id")).where(cb.equal(e.get("catalogoItemId"), item.get("id")),
                        cb.or(cb.like(cb.lower(e.get("barcode")), texto, '\\'),
                                cb.like(cb.lower(item.get("titulo")), texto, '\\')));
                condiciones.add(cb.or(p.get("prestatarioId").in(personas), p.get("ejemplarId").in(copias)));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
        var orden = Sort.by(Sort.Order.desc("fechaHoraEntrega"), Sort.Order.desc("id"));
        // Los indicadores se calculan sobre todo el filtro, no sobre la página.
        long total = repositorio.count(spec);
        long activos = repositorio.count(spec.and((p, q, cb) -> cb.equal(p.get("estado"), EstadoPrestamo.ACTIVO)));
        long vencidos = repositorio.count(spec.and((p, q, cb) -> cb.and(
                cb.equal(p.get("estado"), EstadoPrestamo.ACTIVO), cb.lessThan(p.get("fechaHoraVencimiento"), filtro.ahora()))));
        long tardias = repositorio.count(spec.and((p, q, cb) -> cb.greaterThan(p.get("fechaHoraDevolucion"), p.get("fechaHoraVencimiento"))));
        var pagina = filtro.completo() ? repositorio.findAll(spec, orden)
                : repositorio.findAll(spec, PageRequest.of(filtro.pagina(), filtro.tamano(), orden)).getContent();
        return new Resultado(pagina.stream().map(PrestamoJpaAdapter::aDominio).toList(), total, activos, vencidos, tardias);
    }
}
