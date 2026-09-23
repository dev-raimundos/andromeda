package br.app.coeur.authentication.service;

import br.app.coeur.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final long EXPIRES_IN_SECONDS = 900;

    private final JwtEncoder jwtEncoder;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("coeur-api")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(EXPIRES_IN_SECONDS))
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("scope", user.getRoles())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public long getAccessTokenExpiresIn() {
        return EXPIRES_IN_SECONDS;
    }
}
