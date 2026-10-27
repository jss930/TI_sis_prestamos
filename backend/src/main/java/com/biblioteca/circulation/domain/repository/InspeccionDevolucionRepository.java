package com.biblioteca.circulation.domain.repository;

import com.biblioteca.circulation.domain.model.InspeccionDevolucion;
import java.util.List;

public interface InspeccionDevolucionRepository {
    void guardar(InspeccionDevolucion inspeccion);
    List<InspeccionDevolucion> listar();
}
