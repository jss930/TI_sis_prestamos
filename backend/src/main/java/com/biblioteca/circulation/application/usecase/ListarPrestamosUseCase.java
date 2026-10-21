package com.biblioteca.circulation.application.usecase;

import com.biblioteca.circulation.domain.model.EstadoPrestamo;
import com.biblioteca.circulation.domain.model.Prestamo;
import com.biblioteca.circulation.domain.repository.PrestamoRepository;
import com.biblioteca.identity.domain.vo.UsuarioId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarPrestamosUseCase {

    private final PrestamoRepository prestamos;

    public ListarPrestamosUseCase(PrestamoRepository prestamos) {
        this.prestamos = prestamos;
    }

    public List<Prestamo> ejecutar(UsuarioId prestatarioId, EstadoPrestamo estado) {
        List<Prestamo> base = prestatarioId == null
                ? prestamos.listarTodos()
                : prestamos.listarPorPrestatario(prestatarioId);
        return base.stream()
                .filter(prestamo -> estado == null || prestamo.estado() == estado)
                .toList();
    }
}
