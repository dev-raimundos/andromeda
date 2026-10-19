package br.app.coeur.shopping.application;

import br.app.coeur.shopping.application.dto.request.CreateItemRequest;
import br.app.coeur.shopping.application.dto.request.UpdateItemRequest;
import br.app.coeur.shopping.application.dto.response.ItemResponse;
import br.app.coeur.shopping.domain.Item;
import br.app.coeur.shopping.domain.ItemRepository;
import br.app.coeur.shopping.domain.exception.ItemNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ItemResponse findById(Long id) {
        return ItemResponse.from(getItem(id));
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> findAll(Pageable pageable) {
        return itemRepository.findAll(pageable).map(ItemResponse::from);
    }

    @Transactional
    public ItemResponse update(Long id, UpdateItemRequest request) {
        Item item = getItem(id);

        item.rename(request.name());
        item.changeDescription(request.description());
        item.changePrice(request.price());
        item.changeUrl(request.url());
        item.changeBrand(request.brand());

        return ItemResponse.from(item);
    }

    @Transactional
    public void delete(Long id) {
        itemRepository.delete(
                getItem(id)
        );
    }

    private Item getItem(Long id) {
        return itemRepository.findById(id).orElseThrow(ItemNotFoundException::new);
    }
}
