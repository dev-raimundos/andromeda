package br.app.coeur.shopping.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ShoppingListRepository {
    ShoppingList save(ShoppingList shoppingList);

    Optional<ShoppingList> findById(Long id);

    Page<ShoppingList> findAllByUserId(Long userId, Pageable pageable);

    Optional<ShoppingList> findByIdAndUserId(Long id, Long userId);

    void delete(ShoppingList shoppingList);
}
