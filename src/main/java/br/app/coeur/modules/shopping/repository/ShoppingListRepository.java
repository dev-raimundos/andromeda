package br.app.coeur.modules.shopping.repository;

import br.app.coeur.modules.shopping.model.ShoppingList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    Page<ShoppingList> findAllByUserId(Long userId, Pageable pageable);
    Optional<ShoppingList> findByIdAndUserId(Long id, Long userId);
}
