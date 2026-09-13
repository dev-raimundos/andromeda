package br.app.coeur.authentication.application.usecase;

import br.app.coeur.authentication.application.abstractions.TokenService;
import br.app.coeur.authentication.application.dto.LoginRequest;
import br.app.coeur.authentication.application.dto.TokenResponse;
import br.app.coeur.authentication.domain.RefreshToken;
import br.app.coeur.authentication.domain.abstractions.RefreshTokenRepository;
import br.app.coeur.users.application.UserAppService;
import br.app.coeur.users.application.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthenticateUserUseCase {

    private final UserAppService userAppService;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse execute(LoginRequest request) {
        UserResponse user = userAppService.authenticate(request.getEmail(), request.getPassword())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        // Revoca os tokens de atualização anteriores deste usuário
        refreshTokenRepository.revokeAllByUserId(user.getId());

        String accessToken = tokenService.generateAccessToken(user);
        String refreshTokenString = tokenService.generateRefreshToken();

        // Cria o novo Refresh Token (válido por 7 dias)
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
