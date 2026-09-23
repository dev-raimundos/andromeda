package br.app.coeur.user.infrastructure.web;

import br.app.coeur.user.application.usecase.UserOutput;
import br.app.coeur.user.application.usecase.finduserbyid.FindUserByIdUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento (CRUD) de usuários no sistema")
public class GetMeController {

    private final FindUserByIdUseCase findUserByIdUseCase;

    @GetMapping("/me")
    @Operation(
            summary = "Obter dados do usuário logado",
            description = "Recupera os detalhes do perfil do usuário autenticado no momento a partir do JWT."
    )
    @ApiResponse(responseCode = "200", description = "Dados obtidos com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autorizado / Token JWT ausente ou inválido")
    public UserOutput me(@AuthenticationPrincipal Jwt jwt) {
        Long id = Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
        return findUserByIdUseCase.execute(id);
    }
}
