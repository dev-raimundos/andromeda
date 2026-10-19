package br.app.coeur.shopping.infrastructure.persistence;

import br.app.coeur.shopping.domain.ShoppingList;
import br.app.coeur.shopping.domain.ShoppingListRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingListJpaRepository extends ShoppingListRepository, JpaRepository<ShoppingList, Long> {
}
