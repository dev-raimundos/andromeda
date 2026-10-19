package br.app.coeur.shopping.infrastructure.persistence;

import br.app.coeur.shopping.domain.Item;
import br.app.coeur.shopping.domain.ItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemJpaRepository extends ItemRepository, JpaRepository<Item, Long> {
}
