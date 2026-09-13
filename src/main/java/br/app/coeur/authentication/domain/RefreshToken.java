package br.app.coeur.authentication.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private Long id;
    private String token;
    private Long userId;
    private Instant expiryDate;
    private boolean revoked;

    public boolean isExpired(Instant now) {
        return expiryDate.isBefore(now);
    }

    public boolean isValid(Instant now) {
        return !revoked && !isExpired(now);
    }
}
