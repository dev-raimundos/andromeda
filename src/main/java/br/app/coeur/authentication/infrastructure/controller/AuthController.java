package br.app.coeur.authentication.infrastructure.controller;

import br.app.coeur.authentication.application.usecase.login.LoginRequest;
import br.app.coeur.authentication.application.usecase.refresh.RefreshRequest;
import br.app.coeur.authentication.application.dto.TokenResponse;
import br.app.coeur.authentication.application.usecase.login.AuthenticateUserUseCase;
import br.app.coeur.authentication.application.usecase.refresh.RefreshTokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authenticateUserUseCase.execute(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        return refreshTokenUseCase.execute(request);
    }
}
