package com.biblioteca.circulation.application.port;

import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.identity.domain.vo.UsuarioId;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import java.time.Instant;
import java.util.List;

/** Lectura paginada sin dependencias de Spring Data en aplicación/dominio. */
public interface HistorialPrestamosPort {
    Resultado consultar(Filtro filtro);
    record Filtro(UsuarioId usuario, EjemplarId ejemplar, Instant desde, Instant hastaExclusivo,
                  EstadoPrestamo estado, String busqueda, int pagina, int tamano, boolean completo, Instant ahora) { }
    record Resultado(List<Prestamo> registros, long total, long activos, long vencidos, long devolucionesTardias) { }
}
