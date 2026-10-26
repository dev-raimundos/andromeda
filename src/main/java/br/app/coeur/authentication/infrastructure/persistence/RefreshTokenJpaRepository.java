package br.app.coeur.authentication.infrastructure.persistence;

import br.app.coeur.authentication.domain.RefreshToken;
import br.app.coeur.authentication.domain.RefreshTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RefreshTokenJpaRepository extends RefreshTokenRepository, JpaRepository<RefreshToken, UUID> {

    @Override
    @Modifying(flushAutomatically = true)
    @Query("UPDATE RefreshToken t SET t.revoked = true WHERE t.userId = :userId AND t.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId);
}
