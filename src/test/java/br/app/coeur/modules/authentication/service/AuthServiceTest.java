package br.app.coeur.modules.authentication.service;

import br.app.coeur.shared.exception.BusinessException;
import br.app.coeur.shared.exception.ResourceNotFoundException;
import br.app.coeur.modules.authentication.domain.RefreshToken;
import br.app.coeur.modules.authentication.dto.LoginRequest;
import br.app.coeur.modules.authentication.dto.RefreshTokenRequest;
import br.app.coeur.modules.authentication.dto.TokenResponse;
import br.app.coeur.modules.authentication.repository.RefreshTokenRepository;
import br.app.coeur.modules.user.domain.User;
import br.app.coeur.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.register("john@coeur.app", "encoded", "John");
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    private void stubTokenGeneration() {
        when(tokenService.generateAccessToken(user)).thenReturn("access-token");
        when(tokenService.generateRefreshToken()).thenReturn("refresh-token");
        when(tokenService.getAccessTokenExpiresIn()).thenReturn(900L);
    }

    // ---------- login ----------

    @Test
    void loginShouldRevokeOldTokensAndIssueNewPair() {
        when(userRepository.findByEmail("john@coeur.app")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        stubTokenGeneration();

        TokenResponse response = authService.login(new LoginRequest("john@coeur.app", "secret"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);

        verify(refreshTokenRepository).revokeAllByUserId(1L);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("refresh-token");
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().isValid(Instant.now())).isTrue();
    }

    @Test
    void loginShouldResetPreviousFailedAttempts() {
        user.registerFailedLogin(Instant.now());
        user.registerFailedLogin(Instant.now());
        when(userRepository.findByEmail("john@coeur.app")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        stubTokenGeneration();

        authService.login(new LoginRequest("john@coeur.app", "secret"));

        assertThat(user.getFailedAttempts()).isZero();
    }

    @Test
    void loginShouldFailForUnknownEmail() {
        when(userRepository.findByEmail("ghost@coeur.app")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@coeur.app", "secret")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciais inválidas.");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginShouldFailAndCountAttemptWhenPasswordIsWrong() {
        when(userRepository.findByEmail("john@coeur.app")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("john@coeur.app", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciais inválidas.");

        assertThat(user.getFailedAttempts()).isEqualTo(1);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginShouldFailWhenPasswordIsNull() {
        when(userRepository.findByEmail("john@coeur.app")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("john@coeur.app", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Credenciais inválidas.");

        assertThat(user.getFailedAttempts()).isEqualTo(1);
    }

    @Test
    void loginShouldLockAccountOnFifthFailedAttempt() {
        for (int i = 0; i < User.MAX_FAILED_ATTEMPTS - 1; i++) {
            user.registerFailedLogin(Instant.now());
        }
        when(userRepository.findByEmail("john@coeur.app")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("john@coeur.app", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("bloqueada");

        assertThat(user.isLocked(Instant.now())).isTrue();
    }

    @Test
    void loginShouldRejectLockedAccountWithoutCheckingPassword() {
        user.registerFailedLogin(Instant.now().minus(Duration.ofMinutes(1)));
        ReflectionTestUtils.setField(user, "lockExpiredAt", Instant.now().plus(Duration.ofMinutes(10)));
        when(userRepository.findByEmail("john@coeur.app")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("john@coeur.app", "secret")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("bloqueada");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(refreshTokenRepository, never()).save(any());
    }

    // ---------- refresh ----------

    @Test
    void refreshShouldRotateTokens() {
        RefreshToken oldToken = RefreshToken.issue("old-token", 1L, Instant.now(), Duration.ofDays(7));
        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(oldToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        stubTokenGeneration();

        TokenResponse response = authService.refresh(new RefreshTokenRequest("old-token"));

        assertThat(oldToken.isRevoked()).isTrue();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("refresh-token");
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
    }

    @Test
    void refreshShouldFailWhenTokenDoesNotExist() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("unknown")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token inválido, revogado ou expirado.");
    }

    @Test
    void refreshShouldFailWhenTokenIsRevoked() {
        RefreshToken revoked = RefreshToken.issue("revoked-token", 1L, Instant.now(), Duration.ofDays(7));
        revoked.revoke();
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("revoked-token")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token inválido, revogado ou expirado.");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshShouldFailWhenTokenIsExpired() {
        RefreshToken expired = RefreshToken.issue("expired-token", 1L, Instant.now().minus(Duration.ofDays(10)), Duration.ofDays(7));
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("expired-token")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Refresh token inválido, revogado ou expirado.");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshShouldFailWhenUserNoLongerExists() {
        RefreshToken token = RefreshToken.issue("orphan-token", 1L, Instant.now(), Duration.ofDays(7));
        when(refreshTokenRepository.findByToken("orphan-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("orphan-token")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário não encontrado.");

        assertThat(token.isRevoked()).isFalse();
    }
}
