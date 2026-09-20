package br.app.coeur.users.controller;

import br.app.coeur.users.dto.UserRegisterRequest;
import br.app.coeur.users.dto.UserResponse;
import br.app.coeur.users.usecase.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento (CRUD) de usuários no sistema")
public class RegisterUserController {

    private final RegisterUserUseCase registerUserUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar um novo usuário",
            description = "Cria um novo usuário comum no banco de dados. Endpoint público."
    )
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "E-mail já está em uso ou payload inválido")
    public UserResponse register(@RequestBody UserRegisterRequest request) {
        return registerUserUseCase.execute(request);
    }
}
