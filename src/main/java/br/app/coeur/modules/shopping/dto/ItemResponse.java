package br.app.coeur.modules.shopping.dto;

import br.app.coeur.modules.shopping.model.Item;

public record ItemResponse(
        Long id,
        String name,
        String description,
        Double price,
        String url,
        String brand) {

    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getUrl(),
                item.getBrand()
        );
    }
}
