package br.app.coeur.shopping.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddItemToShoppingListRequest(
        @NotNull(message = "Item is required.")
        Long itemId,

        @NotNull(message = "Quantity is required.")
        @Positive(message = "Quantity must be greater than zero.")
        Integer quantity) {
}
