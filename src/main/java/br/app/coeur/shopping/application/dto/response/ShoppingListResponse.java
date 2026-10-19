package br.app.coeur.shopping.application.dto.response;

import br.app.coeur.shopping.domain.ShoppingList;

import java.util.List;

public record ShoppingListResponse(
        Long id,
        String name,
        Long userId,
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
