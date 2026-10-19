package br.app.coeur.shopping.application.dto.response;

import br.app.coeur.shopping.domain.Item;

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
