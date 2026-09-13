package br.app.coeur.users.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {
    @Id
    @Column("id")
    private Long id;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("name")
    private String name;

    @Column("roles")
    private String roles;

    @Column("failed_attempts")
    private int failedAttempts;

    @Column("lock_expired_at")
    private Instant lockExpiredAt;

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
