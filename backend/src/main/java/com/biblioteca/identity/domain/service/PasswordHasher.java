package com.biblioteca.identity.domain.service;

/** Puerto de dominio para hashear y verificar contraseñas. */
public interface PasswordHasher {
    String hash(String raw);
    boolean matches(String raw, String stored);
}
