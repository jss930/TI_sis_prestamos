package com.biblioteca.inventory.domain.model;

import com.biblioteca.catalog.domain.vo.CatalogoItemId;
import com.biblioteca.inventory.domain.vo.Barcode;
import com.biblioteca.inventory.domain.vo.EjemplarId;
import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EjemplarTest {

    private static final CatalogoItemId ITEM_ID = new CatalogoItemId(UUID.randomUUID());

    @Test
    void registraUnaUnidadDisponible() {
        var ejemplar = Ejemplar.registrar(ITEM_ID, new Barcode("LIB-001"),
                new Ubicacion("Sala", "A-1"));

        assertThat(ejemplar.estaDisponible()).isTrue();
        assertThat(ejemplar.estadoFisico()).isEqualTo(EstadoFisico.DISPONIBLE);
        assertThat(ejemplar.eventosDeDominio()).hasSize(1);
    }

    @Test
    void prestarYDevolverCambianElEstado() {
        var ejemplar = disponible();

        ejemplar.prestar();
        assertThat(ejemplar.estadoFisico()).isEqualTo(EstadoFisico.PRESTADO);

        ejemplar.devolver(EstadoFisico.DISPONIBLE);
        assertThat(ejemplar.estadoFisico()).isEqualTo(EstadoFisico.DISPONIBLE);
    }

    @Test
    void noSePrestaDosVeces() {
        var ejemplar = disponible();
        ejemplar.prestar();

        assertThatThrownBy(ejemplar::prestar)
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("EJEMPLAR_NO_DISPONIBLE"));
    }

    @Test
    void noSeDevuelveSiNoEstaPrestado() {
        var ejemplar = disponible();

        assertThatThrownBy(() -> ejemplar.devolver(EstadoFisico.DISPONIBLE))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.codigo()).isEqualTo("EJEMPLAR_NO_PRESTADO"));
    }

    @Test
    void devolucionDanadaRegistraElDano() {
        var ejemplar = disponible();
        ejemplar.prestar();

        ejemplar.devolver(EstadoFisico.DANADO);

        assertThat(ejemplar.estadoFisico()).isEqualTo(EstadoFisico.DANADO);
    }

    @Test
    void noSeDaDeBajaUnEjemplarPrestado() {
        var ejemplar = disponible();
        ejemplar.prestar();

        assertThatThrownBy(() -> ejemplar.darDeBaja("Perdido"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void barcodeSeNormalizaAMayusculas() {
        assertThat(new Barcode(" lib-001 ").value()).isEqualTo("LIB-001");
    }

    @Test
    void barcodeRechazaCaracteresInvalidos() {
        assertThatThrownBy(() -> new Barcode("LIB 001!"))
                .isInstanceOf(DomainException.class);
    }

    private static Ejemplar disponible() {
        return Ejemplar.reconstruir(
                new EjemplarId(UUID.randomUUID()),
                ITEM_ID,
                new Barcode("LIB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()),
                new Ubicacion("Sala", "A-1"),
                EstadoFisico.DISPONIBLE,
                null,
                Instant.now());
    }
}
