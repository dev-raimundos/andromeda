package br.app.coeur.core.config;

import br.app.coeur.shared.exception.BusinessException;
import br.app.coeur.shared.exception.ConflictException;
import br.app.coeur.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
    void shouldHideInternalDetailsOnUnexpectedError() {
        ProblemDetail problem = handler.handleUnexpected(new IllegalStateException("senha do banco: 1234"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).doesNotContain("1234");
    }
}
