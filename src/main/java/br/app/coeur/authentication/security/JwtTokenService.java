package br.app.coeur.authentication.security;

import br.app.coeur.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService implements TokenService {

    private final JwtEncoder jwtEncoder;
    private static final long EXPIRES_IN_SECONDS = 900; // 15 minutos

    @Override
    public String generateAccessToken(UserResponse user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("coeur-api")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(EXPIRES_IN_SECONDS))
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("scope", user.getRoles()) // "scope" ou "scp" é o padrão lido pelo JwtAuthenticationConverter
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public long getAccessTokenExpiresIn() {
        return EXPIRES_IN_SECONDS;
    }
}
