package br.app.coeur.authentication.usecase;

import br.app.coeur.authentication.dto.RefreshRequest;
import br.app.coeur.authentication.dto.TokenResponse;
import br.app.coeur.authentication.model.RefreshToken;
import br.app.coeur.authentication.repository.RefreshTokenRepository;
import br.app.coeur.authentication.security.TokenService;
import br.app.coeur.users.application.UserAppService;
import br.app.coeur.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserAppService userAppService;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse execute(RefreshRequest request) {
        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .filter(token -> token.isValid(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido, revogado ou expirado."));

        UserResponse user = userAppService.findById(oldRefreshToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);

        String newAccessToken = tokenService.generateAccessToken(user);
        String newRefreshTokenString = tokenService.generateRefreshToken();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenString)
                .userId(user.getId())
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .tokenType("Bearer")
                .expiresIn(tokenService.getAccessTokenExpiresIn())
                .build();
    }
}
