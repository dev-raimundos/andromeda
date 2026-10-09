package br.app.coeur.modules.shopping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateItemRequest(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "Descrição é obrigatória.")
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres.")
        String description,

        @NotNull(message = "Preço é obrigatório.")
        @PositiveOrZero(message = "Preço não pode ser negativo.")
        Double price,

        String url,

        @NotBlank(message = "Marca é obrigatória.")
        @Size(max = 100, message = "Marca deve ter no máximo 100 caracteres.")
        String brand) {
}
