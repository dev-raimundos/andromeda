package br.app.coeur.authentication.domain.abstractions;

import br.app.coeur.authentication.domain.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void revokeAllByUserId(Long userId);
}
