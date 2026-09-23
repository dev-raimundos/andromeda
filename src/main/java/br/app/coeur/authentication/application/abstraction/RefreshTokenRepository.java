package br.app.coeur.authentication.application.abstraction;

import br.app.coeur.authentication.domain.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken save(RefreshToken refreshToken);
    void revokeAllByUserId(Long userId);
}
