package br.app.coeur.shopping.application.dto.response;

import br.app.coeur.shopping.domain.Item;

import java.util.UUID;

public record ItemResponse(
        UUID id,
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
