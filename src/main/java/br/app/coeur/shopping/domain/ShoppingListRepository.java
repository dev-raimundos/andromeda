package br.app.coeur.shopping.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingListRepository {
    ShoppingList save(ShoppingList shoppingList);

    Optional<ShoppingList> findById(UUID id);

    Page<ShoppingList> findAllByUserId(UUID userId, Pageable pageable);

    Optional<ShoppingList> findByIdAndUserId(UUID id, UUID userId);

    void delete(ShoppingList shoppingList);
}
