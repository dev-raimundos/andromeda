package br.app.coeur.shopping.application.dto.response;

import br.app.coeur.shopping.domain.ShoppingListItem;

import java.util.UUID;

public record ItemListResponse(
        UUID id,
        ItemResponse item,
        int quantity,
        boolean purchased) {

    public static ItemListResponse from(ShoppingListItem shoppingListItem) {
        return new ItemListResponse(
                shoppingListItem.getId(),
                ItemResponse.from(shoppingListItem.getItem()),
                shoppingListItem.getQuantity(),
                shoppingListItem.isPurchased()
        );
    }
}
