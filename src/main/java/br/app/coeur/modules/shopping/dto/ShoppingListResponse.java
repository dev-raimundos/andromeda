package br.app.coeur.modules.shopping.dto;

import br.app.coeur.modules.shopping.model.ShoppingList;

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
