package br.app.coeur.core.config;

import br.app.coeur.shared.exception.BusinessException;
import br.app.coeur.shared.exception.ConflictException;
import br.app.coeur.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapResourceNotFoundToNotFound() {
        ProblemDetail problem = handler.handleResourceNotFound(new ResourceNotFoundException("Usuário não encontrado."));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getDetail()).isEqualTo("Usuário não encontrado.");
    }

    @Test
    void shouldMapConflictToConflict() {
        ProblemDetail problem = handler.handleConflict(new ConflictException("E-mail já está em uso."));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getDetail()).isEqualTo("E-mail já está em uso.");
    }

    @Test
    void shouldMapBusinessToBadRequest() {
        ProblemDetail problem = handler.handleBusiness(new BusinessException("Credenciais inválidas."));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Credenciais inválidas.");
    }

    @Test
    void shouldMapIllegalArgumentToBadRequest() {
        ProblemDetail problem = handler.handleIllegalArgument(new IllegalArgumentException("Nome é obrigatório."));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Nome é obrigatório.");
    }

    @Test
    void shouldListInvalidFieldsOnValidationError() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "E-mail inválido."));
        MethodParameter parameter = new MethodParameter(Object.class.getMethod("toString"), -1);

        ProblemDetail problem = handler.handleValidation(new MethodArgumentNotValidException(parameter, bindingResult));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getProperties()).containsKey("errors");
        assertThat(problem.getProperties().get("errors")).isEqualTo(Map.of("email", "E-mail inválido."));
    }

    @Test
    void shouldMapUnreadableMessageToBadRequestWithoutLeakingParserDetails() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error: Unexpected character at com.fasterxml.jackson.core.JsonParser",
                new MockHttpInputMessage(new byte[0]));

        ProblemDetail problem = handler.handleUnreadableMessage(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Corpo da requisição ausente ou malformado.");
    }

    @Test
    void shouldMapTypeMismatchToBadRequestNamingTheParameter() throws Exception {
        MethodParameter parameter = new MethodParameter(Long.class.getMethod("valueOf", String.class), 0);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", parameter, new NumberFormatException("For input string: \"abc\""));

        ProblemDetail problem = handler.handleTypeMismatch(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Parâmetro 'id' com valor inválido.");
    }

    @Test
    void shouldHideInternalDetailsOnUnexpectedError() {
        ProblemDetail problem = handler.handleUnexpected(new IllegalStateException("senha do banco: 1234"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).doesNotContain("1234");
    }
}
