package br.app.coeur.modules.shopping.repository;

import br.app.coeur.modules.shopping.model.ShoppingList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, UUID> {
    Page<ShoppingList> findAllByUserId(UUID userId, Pageable pageable);
    Optional<ShoppingList> findByIdAndUserId(UUID id, UUID userId);
}
