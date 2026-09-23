package br.app.coeur.authentication.application.usecase.refreshtoken;

import br.app.coeur.authentication.application.abstraction.RefreshTokenRepository;
import br.app.coeur.authentication.application.abstraction.TokenService;
import br.app.coeur.authentication.application.usecase.TokenOutput;
import br.app.coeur.authentication.domain.RefreshToken;
import br.app.coeur.user.application.abstraction.UserRepository;
import br.app.coeur.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenOutput execute(RefreshTokenInput input) {

        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(input.getRefreshToken())
                .filter(
                        token -> token.isValid(Instant.now())
                ).orElseThrow(
                        () -> new IllegalArgumentException("Refresh token inválido, revogado ou expirado.")
                );

        User user = userRepository.findById(oldRefreshToken.getUserId()).orElseThrow(
                () -> new IllegalArgumentException("Usuário não encontrado.")
        );

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

        return TokenOutput.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .tokenType("Bearer")
                .expiresIn(tokenService.getAccessTokenExpiresIn())
                .build();
    }
}
