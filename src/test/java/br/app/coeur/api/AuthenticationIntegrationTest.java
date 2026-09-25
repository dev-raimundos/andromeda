package br.app.coeur.api;

import br.app.coeur.modules.authentication.dto.LoginRequest;
import br.app.coeur.modules.authentication.dto.RefreshTokenRequest;
import br.app.coeur.modules.user.dto.RegisterUserRequest;
import br.app.coeur.modules.user.dto.UpdateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldPerformFullAuthenticationLifecycle() throws Exception {
        // 1. Cadastrar um usuário
        RegisterUserRequest registerRequest = new RegisterUserRequest("test@coeur.app", "secret123", "John Doe");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is("test@coeur.app")))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.roles", is("ROLE_USER")));

        // 2. Tentar acessar /api/users/me sem token (deve dar 401 Unauthorized)
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        // 3. Fazer login e obter o TokenPair
        LoginRequest loginRequest = new LoginRequest("test@coeur.app", "secret123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(900)))
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseContent).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(responseContent).get("refreshToken").asText();

        // 4. Acessar /api/users/me usando o token de acesso (deve retornar os dados do usuário)
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@coeur.app")))
                .andExpect(jsonPath("$.name", is("John Doe")));

        // 5. Utilizar o refresh token para obter novos tokens
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andReturn();

        String newResponseContent = refreshResult.getResponse().getContentAsString();
        String newAccessToken = objectMapper.readTree(newResponseContent).get("accessToken").asText();

        // 6. Tentar usar o refresh token antigo novamente (deve falhar por rotação/revogação)
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isBadRequest());

        // 7. Usar o novo token de acesso para chamar o endpoint protegido
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@coeur.app")));
    }

    @Test
    void shouldLockAccountAfterFiveFailedAttempts() throws Exception {
        // 1. Cadastrar usuário
        RegisterUserRequest registerRequest = new RegisterUserRequest("lock@coeur.app", "secret123", "Locked User");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Tentar login com senha errada por 4 vezes consecutivas (não deve bloquear ainda)
        LoginRequest wrongLoginRequest = new LoginRequest("lock@coeur.app", "wrongpassword");

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongLoginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", is("Credenciais inválidas.")));
        }

        // 3. A 5ª tentativa falha deve ativar o bloqueio temporário
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("bloqueada")));

        // 4. Tentar logar com a senha CORRETA (deve continuar bloqueado)
        LoginRequest correctLoginRequest = new LoginRequest("lock@coeur.app", "secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("bloqueada")));
    }

    @Test
    void shouldPerformFullUserCrud() throws Exception {
        // 1. Cadastrar administrador e obter token para autorização nos endpoints de CRUD
        RegisterUserRequest adminRegister = new RegisterUserRequest("admin@coeur.app", "admin123", "Admin User");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegister)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@coeur.app", "admin123"))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
        String authHeader = "Bearer " + token;

        // 2. Criar um novo usuário via CRUD (registrando outro usuário)
        RegisterUserRequest userRequest = new RegisterUserRequest("crud@coeur.app", "crud123", "Crud User");

        MvcResult createResult = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        long newUserId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 3. Obter usuário pelo ID
        mockMvc.perform(get("/api/users/" + newUserId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("crud@coeur.app")))
                .andExpect(jsonPath("$.name", is("Crud User")));

        // 4. Listar todos os usuários
        mockMvc.perform(get("/api/users")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        // 5. Atualizar dados do usuário
        UpdateUserRequest updateRequest = new UpdateUserRequest("crud_updated@coeur.app", "Crud User Updated", "ROLE_ADMIN");

        mockMvc.perform(put("/api/users/" + newUserId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Crud User Updated")))
                .andExpect(jsonPath("$.email", is("crud_updated@coeur.app")))
                .andExpect(jsonPath("$.roles", is("ROLE_ADMIN")));

        // 6. Deletar usuário pelo ID
        mockMvc.perform(delete("/api/users/" + newUserId)
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        // 7. Obter usuário deletado (deve dar erro 404 Not Found através da nossa exceção)
        mockMvc.perform(get("/api/users/" + newUserId)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("Usuário não encontrado.")));
    }

    @Test
    void shouldRejectInvalidPayloadsWithBadRequest() throws Exception {
        // 1. Cadastro com senha em branco e e-mail inválido
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterUserRequest("invalid", " ", "John"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email", is("E-mail inválido.")))
                .andExpect(jsonPath("$.errors.password", is("Senha é obrigatória.")));

        // 2. Login sem senha
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("john@coeur.app", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password", is("Senha é obrigatória.")));

        // 3. Refresh sem token
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.refreshToken", is("Refresh token é obrigatório.")));

        // 4. Atualização com campos ausentes (requer autenticação)
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterUserRequest("valid@coeur.app", "secret123", "Valid User"))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("valid@coeur.app", "secret123"))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
        long userId = objectMapper.readTree(
                mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                        .andReturn().getResponse().getContentAsString()
        ).get("id").asLong();

        mockMvc.perform(put("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest(null, " ", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email", is("E-mail é obrigatório.")))
                .andExpect(jsonPath("$.errors.name", is("Nome é obrigatório.")))
                .andExpect(jsonPath("$.errors.roles", is("Perfis são obrigatórios.")));
    }

    @Test
    void shouldRejectMalformedRequestsWithBadRequest() throws Exception {
        // 1. JSON quebrado no body
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"john@coeur.app\", "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Corpo da requisição ausente ou malformado.")));

        // 2. ID não numérico no path (requer autenticação)
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterUserRequest("path@coeur.app", "secret123", "Path User"))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("path@coeur.app", "secret123"))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/users/abc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Parâmetro 'id' com valor inválido.")));
    }

    @Test
    void shouldTriggerRateLimitWhenRequestLimitExceeded() throws Exception {
        // Enviar 30 requisições normais rápidas (limite máximo de tokens configurado é 30 por minuto)
        for (int i = 0; i < 30; i++) {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk());
        }

        // A 31ª requisição deve estourar o limite e retornar 429 Too Many Requests
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error", containsString("Rate limit excedido")));
    }
}
