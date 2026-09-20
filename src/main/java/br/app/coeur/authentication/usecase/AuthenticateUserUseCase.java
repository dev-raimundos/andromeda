package br.app.coeur.authentication.usecase;

import br.app.coeur.authentication.dto.LoginRequest;
import br.app.coeur.authentication.dto.TokenResponse;
import br.app.coeur.authentication.model.RefreshToken;
import br.app.coeur.authentication.repository.RefreshTokenRepository;
import br.app.coeur.authentication.security.TokenService;
import br.app.coeur.users.usecase.VerifyUserCredentialsUseCase;
import br.app.coeur.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthenticateUserUseCase {

    private final VerifyUserCredentialsUseCase verifyUserCredentialsUseCase;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse execute(LoginRequest request) {
        UserResponse user;
        try {
            user = verifyUserCredentialsUseCase.execute(request.getEmail(), request.getPassword());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("bloquead") || e.getMessage().contains("múltiplas tentativas")) {
                throw e;
            }
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        refreshTokenRepository.revokeAllByUserId(user.getId());

        String accessToken = tokenService.generateAccessToken(user);
        String refreshTokenString = tokenService.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .userId(user.getId())
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(tokenService.getAccessTokenExpiresIn())
                .build();
    }
}
