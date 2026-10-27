package com.biblioteca.infrastructure.security;

import com.biblioteca.identity.domain.vo.UsuarioId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UsuarioAutenticado {
    private final TokenService tokens;
    public UsuarioAutenticado(TokenService tokens) { this.tokens = tokens; }
    public UsuarioId exigir(String authorization) {
        var id = authorization != null && authorization.startsWith("Bearer ")
                ? tokens.validar(authorization.substring(7)) : null;
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Inicia sesion nuevamente para realizar esta operacion");
        return new UsuarioId(id);
    }
}
