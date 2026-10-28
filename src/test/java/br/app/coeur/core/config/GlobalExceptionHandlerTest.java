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
        ProblemDetail problem = handler.handleResourceNotFound(new ResourceNotFoundException("User not found."));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getDetail()).isEqualTo("User not found.");
    }

    @Test
    void shouldMapConflictToConflict() {
        ProblemDetail problem = handler.handleConflict(new ConflictException("Email is already in use."));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getDetail()).isEqualTo("Email is already in use.");
    }

    @Test
    void shouldMapBusinessToBadRequest() {
        ProblemDetail problem = handler.handleBusiness(new BusinessException("Invalid credentials."));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Invalid credentials.");
    }

    @Test
    void shouldMapIllegalArgumentToBadRequest() {
        ProblemDetail problem = handler.handleIllegalArgument(new IllegalArgumentException("Name is required."));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Name is required.");
    }

    @Test
    void shouldListInvalidFieldsOnValidationError() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Invalid email."));
        MethodParameter parameter = new MethodParameter(Object.class.getMethod("toString"), -1);

        ProblemDetail problem = handler.handleValidation(new MethodArgumentNotValidException(parameter, bindingResult));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getProperties()).containsEntry("errors", Map.of("email", "Invalid email."));
    }

    @Test
    void shouldMapUnreadableMessageToBadRequestWithoutLeakingParserDetails() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error: Unexpected character at com.fasterxml.jackson.core.JsonParser",
                new MockHttpInputMessage(new byte[0]));

        ProblemDetail problem = handler.handleUnreadableMessage(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Missing or malformed request body.");
    }

    @Test
    void shouldMapTypeMismatchToBadRequestNamingTheParameter() throws Exception {
        MethodParameter parameter = new MethodParameter(Long.class.getMethod("valueOf", String.class), 0);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", parameter, new NumberFormatException("For input string: \"abc\""));

        ProblemDetail problem = handler.handleTypeMismatch(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Parameter 'id' has an invalid value.");
    }

    @Test
    void shouldHideInternalDetailsOnUnexpectedError() {
        ProblemDetail problem = handler.handleUnexpected(new IllegalStateException("database password: 1234"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).doesNotContain("1234");
    }
}
