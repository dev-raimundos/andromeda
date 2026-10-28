package br.app.coeur.modules.shopping.service;

import br.app.coeur.modules.shopping.dto.request.CreateItemRequest;
import br.app.coeur.modules.shopping.dto.request.UpdateItemRequest;
import br.app.coeur.modules.shopping.dto.response.ItemResponse;
import br.app.coeur.modules.shopping.exception.ItemNotFoundException;
import br.app.coeur.modules.shopping.model.Item;
import br.app.coeur.modules.shopping.repository.ItemRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public ItemResponse create(@NotNull CreateItemRequest request) {
        Item item = Item.create(
                request.name(),
                request.description(),
                request.price(),
                request.url(),
                request.brand()
        );

        return ItemResponse.from(
                itemRepository.save(item)
        );
    }

    @Transactional(readOnly = true)
    public ItemResponse findById(UUID id) {
        return ItemResponse.from(getItem(id));
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> findAll(Pageable pageable) {
        return itemRepository.findAll(pageable).map(ItemResponse::from);
    }

    @Transactional
    public ItemResponse update(UUID id, UpdateItemRequest request) {
        Item item = getItem(id);

        item.rename(request.name());
        item.changeDescription(request.description());
        item.changePrice(request.price());
        item.changeUrl(request.url());
        item.changeBrand(request.brand());

        return ItemResponse.from(item);
    }

    @Transactional
    public void delete(UUID id) {
        itemRepository.delete(
                getItem(id)
        );
    }

    private Item getItem(UUID id) {
        return itemRepository.findById(id).orElseThrow(ItemNotFoundException::new);
    }
}
