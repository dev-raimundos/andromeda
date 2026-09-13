package br.app.coeur.authentication.infrastructure.persistence;

import br.app.coeur.authentication.domain.RefreshToken;
import br.app.coeur.authentication.domain.abstractions.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcRefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .id(refreshToken.getId())
                .token(refreshToken.getToken())
                .userId(refreshToken.getUserId())
                .expiryDate(refreshToken.getExpiryDate())
                .revoked(refreshToken.isRevoked())
                .build();

        RefreshTokenEntity savedEntity = springRepository.save(entity);
        refreshToken.setId(savedEntity.getId());
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    public void revokeAllByUserId(Long userId) {
        springRepository.revokeAllByUserId(userId);
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return RefreshToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .userId(entity.getUserId())
                .expiryDate(entity.getExpiryDate())
                .revoked(entity.isRevoked())
                .build();
    }
}
