package br.app.coeur.api;

import br.app.coeur.authentication.application.usecase.authenticateuser.AuthenticateUserInput;
import br.app.coeur.authentication.application.usecase.refreshtoken.RefreshTokenInput;
import br.app.coeur.user.application.usecase.registeruser.RegisterUserInput;
import br.app.coeur.user.application.usecase.updateuser.UpdateUserInput;
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
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    public void shouldPerformFullAuthenticationLifecycle() throws Exception {
        // 1. Cadastrar um usuário
        RegisterUserInput registerRequest = RegisterUserInput.builder()
                .email("test@coeur.app")
                .password("secret123")
                .name("John Doe")
                .build();

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
        AuthenticateUserInput loginRequest = AuthenticateUserInput.builder()
                .email("test@coeur.app")
                .password("secret123")
                .build();

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
        RefreshTokenInput refreshRequest = RefreshTokenInput.builder()
                .refreshToken(refreshToken)
                .build();

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
    public void shouldLockAccountAfterFiveFailedAttempts() throws Exception {
        // 1. Cadastrar usuário
        RegisterUserInput registerRequest = RegisterUserInput.builder()
                .email("lock@coeur.app")
                .password("secret123")
                .name("Locked User")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Tentar login com senha errada por 4 vezes consecutivas (não deve bloquear ainda)
        AuthenticateUserInput wrongLoginRequest = AuthenticateUserInput.builder()
                .email("lock@coeur.app")
                .password("wrongpassword")
                .build();

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongLoginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", is("Credenciais inválidas.")));
        }

        // 3. A 5ª tentativa falha deve ativar o bloqueio temporário
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("bloqueada")));

        // 4. Tentar logar com a senha CORRETA (deve continuar bloqueado)
        AuthenticateUserInput correctLoginRequest = AuthenticateUserInput.builder()
                .email("lock@coeur.app")
                .password("secret123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctLoginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("bloqueada")));
    }

    @Test
    public void shouldPerformFullUserCrud() throws Exception {
        // 1. Cadastrar administrador e obter token para autorização nos endpoints de CRUD
        RegisterUserInput adminRegister = RegisterUserInput.builder()
                .email("admin@coeur.app")
                .password("admin123")
                .name("Admin User")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegister)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AuthenticateUserInput.builder()
                                .email("admin@coeur.app")
                                .password("admin123")
                                .build())))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
        String authHeader = "Bearer " + token;

        // 2. Criar um novo usuário via CRUD (registrando outro usuário)
        RegisterUserInput userRequest = RegisterUserInput.builder()
                .email("crud@coeur.app")
                .password("crud123")
                .name("Crud User")
                .build();

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
        UpdateUserInput updateRequest = UpdateUserInput.builder()
                .name("Crud User Updated")
                .email("crud_updated@coeur.app")
                .roles("ROLE_ADMIN")
                .build();

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

        // 7. Obter usuário deletado (deve dar erro 400 Bad Request através da nossa exceção)
        mockMvc.perform(get("/api/users/" + newUserId)
                        .header("Authorization", authHeader))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Usuário não encontrado.")));
    }

    @Test
    public void shouldTriggerRateLimitWhenRequestLimitExceeded() throws Exception {
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
