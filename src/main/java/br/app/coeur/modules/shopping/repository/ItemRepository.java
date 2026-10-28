package br.app.coeur.modules.shopping.repository;

import br.app.coeur.modules.shopping.model.Item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {
}
