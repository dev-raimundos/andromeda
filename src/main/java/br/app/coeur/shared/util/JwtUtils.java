package br.app.coeur.shared.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;
import java.util.UUID;

public class JwtUtils {
    private JwtUtils() {
        /* This utility class must not be instantiated. */
    }

    public static UUID userId(Jwt jwt) {
        return UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
    }
}
