package br.app.coeur.users.controller;

import br.app.coeur.users.dto.UserResponse;
import br.app.coeur.users.usecase.FindUserByIdUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento (CRUD) de usuários no sistema")
public class GetUserByIdController {

    private final FindUserByIdUseCase findUserByIdUseCase;

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário pelo ID",
            description = "Retorna os detalhes de um usuário específico por meio do seu ID único."
    )
    @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public UserResponse getById(@PathVariable Long id) {
        try {
            return findUserByIdUseCase.execute(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
    }
}
