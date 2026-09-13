package br.app.coeur.authentication.repository;

import br.app.coeur.authentication.model.RefreshToken;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends ListCrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE refresh_tokens SET revoked = true WHERE user_id = :userId AND revoked = false")
    void revokeAllByUserId(Long userId);
}
