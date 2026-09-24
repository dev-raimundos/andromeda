package br.app.coeur.modules.authentication.service;

import br.app.coeur.modules.authentication.domain.RefreshToken;
import br.app.coeur.shared.exception.BusinessException;
import br.app.coeur.shared.exception.ResourceNotFoundException;
import br.app.coeur.modules.user.domain.User;
import br.app.coeur.modules.authentication.dto.LoginRequest;
import br.app.coeur.modules.authentication.dto.RefreshTokenRequest;
import br.app.coeur.modules.authentication.dto.TokenResponse;
import br.app.coeur.modules.authentication.repository.RefreshTokenRepository;
import br.app.coeur.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(7);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(noRollbackFor = BusinessException.class)
    public TokenResponse login(LoginRequest request) {
        User user = verifyCredentials(
                request.email(),
                request.password()
        );

        refreshTokenRepository.revokeAllByUserId(user.getId());

        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(request.refreshToken())
                .filter(token -> token.isValid(Instant.now()))
                .orElseThrow(() -> new BusinessException("Refresh token inválido, revogado ou expirado."));

        User user = userRepository.findById(oldToken.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("Usuário não encontrado.")
        );

        oldToken.revoke();

        return issueTokens(user);
    }

    private User verifyCredentials(String email, String rawPassword) {

        log.info("[LOGIN] Tentativa de login iniciada para o e-mail: '{}'", email);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> {
                    log.warn("[LOGIN] Falha no login: e-mail '{}' não está cadastrado no sistema", email);
                    return new BusinessException("Credenciais inválidas.");
                });

        Instant now = Instant.now();

        if (user.isLocked(now)) {
            log.warn("[LOGIN] Falha no login: conta do usuário '{}' " +
                    "está bloqueada temporariamente até {}", email, user.getLockExpiredAt());
            throw new BusinessException("Conta temporariamente bloqueada devido a múltiplas tentativas falhas. " +
                    "Tente novamente mais tarde.");
        }

        if (rawPassword != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.info("[LOGIN] Login bem-sucedido para o usuário: '{}' (ID: {})", email, user.getId());
            user.registerSuccessfulLogin();
            return user;
        }

        user.registerFailedLogin(now);

        if (user.isLocked(now)) {
            log.error("[LOGIN] Falha no login: senha incorreta para o e-mail '{}'. " +
                    "Limite de tentativas atingido! Conta BLOQUEADA temporariamente até {}", email, user.getLockExpiredAt());
            throw new BusinessException("Conta bloqueada temporariamente devido a múltiplas tentativas falhas.");
        }

        log.warn("[LOGIN] Falha no login: senha incorreta para o e-mail '{}'. " +
                "Tentativas falhas consecutivas: {}/{}", email, user.getFailedAttempts(), User.MAX_FAILED_ATTEMPTS);
        throw new BusinessException("Credenciais inválidas.");
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = tokenService.generateAccessToken(user);
        String refreshTokenValue = tokenService.generateRefreshToken();

        refreshTokenRepository.save(
                RefreshToken.issue(
                        refreshTokenValue,
                        user.getId(),
                        Instant.now(),
                        REFRESH_TOKEN_VALIDITY
                )
        );

        return TokenResponse.bearer(accessToken, refreshTokenValue, tokenService.getAccessTokenExpiresIn());
    }
}
