package br.app.coeur.identity.infrastructure.persistence;

import br.app.coeur.identity.domain.RefreshToken;
import br.app.coeur.identity.domain.RefreshTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends RefreshTokenRepository, JpaRepository<RefreshToken, Long> {

    @Override
    @Modifying(flushAutomatically = true)
    @Query("UPDATE RefreshToken t SET t.revoked = true WHERE t.userId = :userId AND t.revoked = false")
    void revokeAllByUserId(@Param("userId") Long userId);
}
