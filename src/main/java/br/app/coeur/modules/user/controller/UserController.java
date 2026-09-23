package br.app.coeur.modules.user.controller;

import br.app.coeur.modules.user.dto.RegisterUserRequest;
import br.app.coeur.modules.user.dto.UpdateUserRequest;
import br.app.coeur.modules.user.dto.UserResponse;
import br.app.coeur.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento (CRUD) de usuários no sistema")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar um novo usuário",
            description = "Cria um novo usuário comum no banco de dados. Endpoint público."
    )
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "E-mail já está em uso ou payload inválido")
    public UserResponse register(@RequestBody RegisterUserRequest request) {
        return userService.register(request);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Obter dados do usuário logado",
            description = "Recupera os detalhes do perfil do usuário autenticado no momento a partir do JWT."
    )
    @ApiResponse(responseCode = "200", description = "Dados obtidos com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autorizado / Token JWT ausente ou inválido")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        Long id = Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
        return userService.findById(id);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma lista com todos os usuários cadastrados. Requer autenticação JWT."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public List<UserResponse> listAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário pelo ID",
            description = "Retorna os detalhes de um usuário específico por meio do seu ID único."
    )
    @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public UserResponse getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar dados do usuário",
            description = "Modifica o e-mail, nome e perfis de um usuário existente."
    )
    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "ID inválido, e-mail já em uso ou dados inconsistentes")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public UserResponse update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Deletar um usuário",
            description = "Remove fisicamente o cadastro de um usuário específico por meio de seu ID único."
    )
    @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso")
    @ApiResponse(responseCode = "400", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
