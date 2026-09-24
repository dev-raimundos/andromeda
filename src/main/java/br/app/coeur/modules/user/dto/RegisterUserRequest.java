package br.app.coeur.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 100, message = "E-mail deve ter no máximo 100 caracteres.")
        String email,

        @NotBlank(message = "Senha é obrigatória.")
        @Size(max = 100, message = "Senha deve ter no máximo 100 caracteres.")
        String password,

        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres.")
        String name) {
}
