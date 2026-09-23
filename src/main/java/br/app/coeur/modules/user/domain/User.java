package br.app.coeur.modules.user.domain;

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
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    public static final String DEFAULT_ROLE = "ROLE_USER";
    public static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String roles;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "lock_expired_at")
    private Instant lockExpiredAt;

    private User(String email, String password, String name) {
        this.email = requireText(email, "E-mail é obrigatório.");
        this.password = requireText(password, "Senha é obrigatória.");
        this.name = requireText(name, "Nome é obrigatório.");
        this.roles = DEFAULT_ROLE;
    }

    public static User register(String email, String encodedPassword, String name) {
        return new User(email, encodedPassword, name);
    }

    public boolean isLocked(Instant now) {
        return lockExpiredAt != null && lockExpiredAt.isAfter(now);
    }

    public void registerFailedLogin(Instant now) {
        this.failedAttempts++;
        if (this.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            this.lockExpiredAt = now.plus(LOCK_DURATION);
        }
    }

    public void registerSuccessfulLogin() {
        this.failedAttempts = 0;
        this.lockExpiredAt = null;
    }

    public void changeEmail(String email) {
        this.email = requireText(email, "E-mail é obrigatório.");
    }

    public void rename(String name) {
        this.name = requireText(name, "Nome é obrigatório.");
    }

    public void changeRoles(String roles) {
        this.roles = requireText(roles, "Perfis são obrigatórios.");
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
