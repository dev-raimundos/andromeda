package br.app.coeur.authentication.infrastructure.persistence;

import br.app.coeur.authentication.application.abstraction.RefreshTokenRepository;
import br.app.coeur.authentication.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJdbcRepository jdbcRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jdbcRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    public void save(RefreshToken refreshToken) {
        jdbcRepository.save(toEntity(refreshToken));
    }

    @Override
    public void revokeAllByUserId(Long userId) {
        jdbcRepository.revokeAllByUserId(userId);
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

    private RefreshTokenEntity toEntity(RefreshToken refreshToken) {
        return RefreshTokenEntity.builder()
                .id(refreshToken.getId())
                .token(refreshToken.getToken())
                .userId(refreshToken.getUserId())
                .expiryDate(refreshToken.getExpiryDate())
                .revoked(refreshToken.isRevoked())
                .build();
    }
}
