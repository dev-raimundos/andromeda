package br.app.coeur.users.controller;

import br.app.coeur.users.dto.UserResponse;
import br.app.coeur.users.dto.UserUpdateRequest;
import br.app.coeur.users.usecase.UpdateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento (CRUD) de usuários no sistema")
public class UpdateUserController {

    private final UpdateUserUseCase updateUserUseCase;

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar dados do usuário",
            description = "Modifica o e-mail, nome e perfis de um usuário existente."
    )
    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "ID inválido, e-mail já em uso ou dados inconsistentes")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public UserResponse update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return updateUserUseCase.execute(id, request);
    }
}
