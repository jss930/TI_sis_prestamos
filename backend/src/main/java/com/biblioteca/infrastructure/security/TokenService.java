package com.biblioteca.infrastructure.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
public class TokenService {

    private static final String SECRET = "biblioteca-unsa-token-secret-change-in-production";
    private static final long EXPIRY_SECONDS = 8 * 60 * 60;

    public String generar(UUID usuarioId, String email) {
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        long exp = Instant.now().getEpochSecond() + EXPIRY_SECONDS;
        String payload = base64Url("{\"sub\":\"" + usuarioId + "\",\"email\":\"" + email + "\",\"exp\":" + exp + "}");
        String signature = sign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    public UUID validar(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        String expected = sign(parts[0] + "." + parts[1]);
        if (!constantTimeEquals(expected, parts[2])) return null;
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            long exp = Long.parseLong(payload.replaceAll(".*\"exp\":", "").replaceAll("[^0-9].*", ""));
            if (Instant.now().getEpochSecond() > exp) return null;
            String sub = payload.replaceAll(".*\"sub\":\"", "").replaceAll("\".*", "");
            return UUID.fromString(sub);
        } catch (Exception ex) {
            return null;
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar el token", ex);
        }
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }
}
