package br.app.coeur.authentication.application.usecase.authenticateuser;

import br.app.coeur.authentication.application.abstraction.RefreshTokenRepository;
import br.app.coeur.authentication.application.abstraction.TokenService;
import br.app.coeur.authentication.application.usecase.TokenOutput;
import br.app.coeur.authentication.application.usecase.verifyusercredentials.VerifyUserCredentialsUseCase;
import br.app.coeur.authentication.domain.RefreshToken;
import br.app.coeur.user.domain.User;
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
    public TokenOutput execute(AuthenticateUserInput input) {

        User user;

        try {
            user = verifyUserCredentialsUseCase.execute(input.getEmail(), input.getPassword());
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

        return TokenOutput.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(tokenService.getAccessTokenExpiresIn())
                .build();
    }
}
