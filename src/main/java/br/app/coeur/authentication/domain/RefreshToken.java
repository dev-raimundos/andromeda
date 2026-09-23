package br.app.coeur.authentication.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Getter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked;

    private RefreshToken(String token, Long userId, Instant expiryDate) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.revoked = false;
    }

    public static RefreshToken issue(String token, Long userId, Instant now, Duration validFor) {
        return new RefreshToken(token, userId, now.plus(validFor));
    }

    public boolean isExpired(Instant now) {
        return expiryDate.isBefore(now);
    }

    public boolean isValid(Instant now) {
        return !revoked && !isExpired(now);
    }

    public void revoke() {
        this.revoked = true;
    }
}
