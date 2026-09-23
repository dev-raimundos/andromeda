package br.app.coeur.user.infrastructure.web;

import br.app.coeur.user.application.usecase.UserOutput;
import br.app.coeur.user.application.usecase.updateuser.UpdateUserInput;
import br.app.coeur.user.application.usecase.updateuser.UpdateUserUseCase;
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
    public UserOutput update(@PathVariable Long id, @RequestBody UpdateUserInput input) {
        return updateUserUseCase.execute(id, input);
    }
}
