package com.biblioteca.catalog.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ISBNTest {

    @Test
    void normalizaYValidaIsbn13() {
        assertThat(new ISBN("978-0-13-235088-4").value()).isEqualTo("9780132350884");
    }

    @Test
    void normalizaYValidaIsbn10() {
        assertThat(new ISBN("0-306-40615-2").value()).isEqualTo("0306406152");
    }

    @Test
    void rechazaChecksumInvalido() {
        assertThatThrownBy(() -> new ISBN("9780132350885"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ISBN");
    }

    @Test
    void rechazaValorVacio() {
        assertThatThrownBy(() -> new ISBN(" "))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void rechazaLongitudDesconocida() {
        assertThatThrownBy(() -> new ISBN("12345"))
                .isInstanceOf(DomainException.class);
    }
}
