package br.app.coeur.modules.shopping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateItemRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must be at most 100 characters.")
        String name,

        @NotBlank(message = "Description is required.")
        @Size(max = 255, message = "Description must be at most 255 characters.")
        String description,

        @NotNull(message = "Price is required.")
        @PositiveOrZero(message = "Price cannot be negative.")
        Double price,

        String url,

        @NotBlank(message = "Brand is required.")
        @Size(max = 100, message = "Brand must be at most 100 characters.")
        String brand) {
}
