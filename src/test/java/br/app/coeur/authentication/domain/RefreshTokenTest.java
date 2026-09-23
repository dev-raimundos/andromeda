package br.app.coeur.authentication.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final Duration VALIDITY = Duration.ofDays(7);

    @Test
    void issueShouldCreateNonRevokedTokenExpiringAfterValidity() {
        RefreshToken token = RefreshToken.issue("abc", 1L, NOW, VALIDITY);

        assertThat(token.getToken()).isEqualTo("abc");
        assertThat(token.getUserId()).isEqualTo(1L);
        assertThat(token.getExpiryDate()).isEqualTo(NOW.plus(VALIDITY));
        assertThat(token.isRevoked()).isFalse();
    }

    @Test
    void shouldBeValidBeforeExpiry() {
        RefreshToken token = RefreshToken.issue("abc", 1L, NOW, VALIDITY);

        assertThat(token.isExpired(NOW.plus(Duration.ofDays(6)))).isFalse();
        assertThat(token.isValid(NOW.plus(Duration.ofDays(6)))).isTrue();
    }

    @Test
    void shouldBeInvalidAfterExpiry() {
        RefreshToken token = RefreshToken.issue("abc", 1L, NOW, VALIDITY);
        Instant afterExpiry = NOW.plus(VALIDITY).plusSeconds(1);

        assertThat(token.isExpired(afterExpiry)).isTrue();
        assertThat(token.isValid(afterExpiry)).isFalse();
    }

    @Test
    void revokeShouldMakeTokenInvalid() {
        RefreshToken token = RefreshToken.issue("abc", 1L, NOW, VALIDITY);

        token.revoke();

        assertThat(token.isRevoked()).isTrue();
        assertThat(token.isValid(NOW)).isFalse();
    }
}
