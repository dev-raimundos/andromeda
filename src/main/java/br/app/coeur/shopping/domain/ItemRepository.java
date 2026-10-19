package br.app.coeur.shopping.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(Long id);

    Page<Item> findAll(Pageable pageable);

    void delete(Item item);
}
