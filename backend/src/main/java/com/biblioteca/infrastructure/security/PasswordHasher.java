package com.biblioteca.infrastructure.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordHasher implements com.biblioteca.identity.domain.service.PasswordHasher {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    public String hash(String raw) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        byte[] key = pbkdf2(raw.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(key);
    }

    public boolean matches(String raw, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        String[] parts = stored.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] key = Base64.getDecoder().decode(parts[1]);
        byte[] candidate = pbkdf2(raw.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        if (candidate.length != key.length) return false;
        int diff = 0;
        for (int i = 0; i < key.length; i++) diff |= key[i] ^ candidate[i];
        return diff == 0;
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo hashear la contraseña", ex);
        }
    }
}
