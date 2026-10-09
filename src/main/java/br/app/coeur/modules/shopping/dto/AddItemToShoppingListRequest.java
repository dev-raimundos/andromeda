package br.app.coeur.modules.shopping.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddItemToShoppingListRequest(
        @NotNull(message = "Item é obrigatório.")
        Long itemId,

        @NotNull(message = "Quantidade é obrigatória.")
        @Positive(message = "Quantidade deve ser maior que zero.")
        Integer quantity) {
}
