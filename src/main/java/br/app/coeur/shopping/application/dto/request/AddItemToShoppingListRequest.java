package br.app.coeur.shopping.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddItemToShoppingListRequest(
        @NotNull(message = "Item is required.")
        UUID itemId,

        @NotNull(message = "Quantity is required.")
        @Positive(message = "Quantity must be greater than zero.")
        Integer quantity) {
}
