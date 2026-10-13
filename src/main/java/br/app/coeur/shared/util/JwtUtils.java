package br.app.coeur.shared.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;

public class JwtUtils {
    private JwtUtils() {
        /* Essa classe utilitária não deve ser instânciada. */
    }

    public static Long userId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }
}
