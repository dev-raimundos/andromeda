package br.app.coeur.authentication.infrastructure.web;

import br.app.coeur.authentication.application.usecase.TokenOutput;
import br.app.coeur.authentication.application.usecase.authenticateuser.AuthenticateUserInput;
import br.app.coeur.authentication.application.usecase.authenticateuser.AuthenticateUserUseCase;
import br.app.coeur.authentication.application.usecase.refreshtoken.RefreshTokenInput;
import br.app.coeur.authentication.application.usecase.refreshtoken.RefreshTokenUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para login e renovação de tokens (JWT)")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/login")
    @Operation(
            summary = "Realizar login do usuário",
            description = "Autentica e-mail/senha e emite um par de tokens (Access Token JWT + Refresh Token)"
    )
    @ApiResponse(responseCode = "200", description = "Login bem-sucedido")
    @ApiResponse(responseCode = "400", description = "Credenciais inválidas ou conta temporariamente bloqueada")
    public TokenOutput login(@RequestBody AuthenticateUserInput input) {
        return authenticateUserUseCase.execute(input);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Renovar Token de Acesso",
            description = "Valida o Refresh Token enviado, revoga-o para segurança de rotação e emite novos tokens"
    )
    @ApiResponse(responseCode = "200", description = "Renovação realizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Refresh token inválido, expirado ou já revogado")
    public TokenOutput refresh(@RequestBody RefreshTokenInput input) {
        return refreshTokenUseCase.execute(input);
    }
}
