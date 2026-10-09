package br.app.coeur.modules.shopping.dto;

import br.app.coeur.modules.shopping.model.ItemList;

public record ItemListResponse(
        Long id,
        ItemResponse item,
        int quantity,
        boolean purchased) {

    public static ItemListResponse from(ItemList itemList) {
        return new ItemListResponse(
                itemList.getId(),
                ItemResponse.from(itemList.getItem()),
                itemList.getQuantity(),
                itemList.isPurchased()
        );
    }
}
