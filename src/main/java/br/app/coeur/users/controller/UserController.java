package br.app.coeur.users.controller;

import br.app.coeur.users.service.UserAppService;
import br.app.coeur.users.dto.UserRegisterRequest;
import br.app.coeur.users.dto.UserResponse;
import br.app.coeur.users.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento (CRUD) de usuários no sistema")
public class UserController {

    private final UserAppService userAppService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar um novo usuário",
            description = "Cria um novo usuário comum no banco de dados. Endpoint público."
    )
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "E-mail já está em uso ou payload inválido")
    public UserResponse register(@RequestBody UserRegisterRequest request) {
        return userAppService.register(request);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Obter dados do usuário logado",
            description = "Recupera os detalhes do perfil do usuário autenticado no momento a partir do JWT."
    )
    @ApiResponse(responseCode = "200", description = "Dados obtidos com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autorizado / Token JWT ausente ou inválido")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        Long id = Long.valueOf(jwt.getSubject());
        return userAppService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma lista com todos os usuários cadastrados. Requer autenticação JWT."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public List<UserResponse> listAll() {
        return userAppService.listAll();
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
        return userAppService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar dados do usuário",
            description = "Modifica o e-mail, nome e perfis de um usuário existente."
    )
    @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "ID inválido, e-mail já em uso ou dados inconsistentes")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public UserResponse update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return userAppService.update(id, request);
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
        userAppService.delete(id);
    }
}
