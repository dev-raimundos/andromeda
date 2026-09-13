package br.app.coeur.users.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String roles; // Comma-separated roles, e.g., "ROLE_USER"
    private int failedAttempts;
    private Instant lockExpiredAt;

    public boolean isNew() {
        return id == null;
    }

    public boolean isLocked(Instant now) {
        return lockExpiredAt != null && lockExpiredAt.isAfter(now);
    }

    public void lock(Instant until) {
        this.lockExpiredAt = until;
    }

    public void incrementFailedAttempts(Instant now) {
        this.failedAttempts++;
        if (this.failedAttempts >= 5) {
            this.lock(now.plus(15, ChronoUnit.MINUTES));
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lockExpiredAt = null;
    }
}
