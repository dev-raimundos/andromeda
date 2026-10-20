package br.app.coeur.shopping.application.dto.response;

import br.app.coeur.shopping.domain.ShoppingList;

import java.util.List;
import java.util.UUID;

public record ShoppingListResponse(
        UUID id,
        String name,
        UUID userId,
        List<ItemListResponse> items) {

    public static ShoppingListResponse from(ShoppingList shoppingList) {
        return new ShoppingListResponse(
                shoppingList.getId(),
                shoppingList.getName(),
                shoppingList.getUserId(),
                shoppingList.getItems().stream()
                        .map(ItemListResponse::from)
                        .toList()
        );
    }
}
