package com.biblioteca.catalog.domain.vo;

import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.ValueObject;

/**
 * ISBN-10 o ISBN-13 validado con digito verificador.
 * Se normaliza sin guiones ni espacios para comparar y almacenar.
 */
public record ISBN(String value) implements ValueObject {

    public ISBN {
        String normalizado = value == null ? "" : value.replace("-", "").replace(" ", "").trim();
        if (!esValido(normalizado)) {
            throw new DomainException("ISBN_INVALIDO", "ISBN invalido: " + value);
        }
        value = normalizado;
    }

    private static boolean esValido(String isbn) {
        if (isbn.length() == 10) {
            return esValidoIsbn10(isbn);
        }
        if (isbn.length() == 13) {
            return esValidoIsbn13(isbn);
        }
        return false;
    }

    private static boolean esValidoIsbn10(String isbn) {
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            char caracter = isbn.charAt(i);
            int digito;
            if (i == 9 && (caracter == 'X' || caracter == 'x')) {
                digito = 10;
            } else if (Character.isDigit(caracter)) {
                digito = Character.digit(caracter, 10);
            } else {
                return false;
            }
            suma += (10 - i) * digito;
        }
        return suma % 11 == 0;
    }

    private static boolean esValidoIsbn13(String isbn) {
        if (!isbn.chars().allMatch(Character::isDigit)) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 13; i++) {
            int digito = Character.digit(isbn.charAt(i), 10);
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        return suma % 10 == 0;
    }
}
