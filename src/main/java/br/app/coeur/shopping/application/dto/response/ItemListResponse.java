package br.app.coeur.shopping.application.dto.response;

import br.app.coeur.shopping.domain.ShoppingListItem;

public record ItemListResponse(
        Long id,
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
