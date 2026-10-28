package br.app.coeur.modules.shopping.service;

import br.app.coeur.modules.shopping.dto.request.AddItemToShoppingListRequest;
import br.app.coeur.modules.shopping.dto.request.CreateShoppingListRequest;
import br.app.coeur.modules.shopping.dto.request.UpdateItemQuantityRequest;
import br.app.coeur.modules.shopping.dto.request.UpdateShoppingListRequest;
import br.app.coeur.modules.shopping.dto.response.ShoppingListResponse;
import br.app.coeur.modules.shopping.exception.ItemNotFoundException;
import br.app.coeur.modules.shopping.exception.ShoppingListNotFoundException;
import br.app.coeur.modules.shopping.model.Item;
import br.app.coeur.modules.shopping.model.ShoppingList;
import br.app.coeur.modules.shopping.repository.ItemRepository;
import br.app.coeur.modules.shopping.repository.ShoppingListRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public ShoppingListResponse create(UUID userId, CreateShoppingListRequest request) {
        ShoppingList shoppingList = ShoppingList.create(request.name(), userId);

        return ShoppingListResponse.from(
                shoppingListRepository.save(shoppingList)
        );
    }

    @Cacheable(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional(readOnly = true)
    public ShoppingListResponse findById(UUID userId, UUID id) {
        return ShoppingListResponse.from(getShoppingList(userId, id));
    }

    @Transactional(readOnly = true)
    public Page<ShoppingListResponse> findAll(UUID userId, Pageable pageable) {
        return shoppingListRepository.findAllByUserId(userId, pageable)
                .map(ShoppingListResponse::from);
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public ShoppingListResponse rename(UUID userId, UUID id, UpdateShoppingListRequest request) {
        ShoppingList shoppingList = getShoppingList(userId, id);
        shoppingList.rename(request.name());
        return ShoppingListResponse.from(shoppingList);
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public void delete(UUID userId, UUID id) {
        shoppingListRepository.delete(
                getShoppingList(userId, id)
        );
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public ShoppingListResponse addItem(UUID userId, UUID id, AddItemToShoppingListRequest request) {
        ShoppingList shoppingList = getShoppingList(userId, id);
        Item item = getItem(request.itemId());

        shoppingList.addItem(item, request.quantity());

        return ShoppingListResponse.from(shoppingList);
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public ShoppingListResponse removeItem(UUID userId, UUID id, UUID itemId) {
        ShoppingList shoppingList = getShoppingList(userId, id);
        Item item = getItem(itemId);

        shoppingList.removeItem(item);

        return ShoppingListResponse.from(shoppingList);
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public ShoppingListResponse changeItemQuantity(UUID userId, UUID id, UUID itemId, UpdateItemQuantityRequest request) {
        ShoppingList shoppingList = getShoppingList(userId, id);
        Item item = getItem(itemId);

        shoppingList.changeItemQuantity(item, request.quantity());

        return ShoppingListResponse.from(shoppingList);
    }

    private ShoppingList getShoppingList(UUID userId, UUID id) {
        return shoppingListRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ShoppingListNotFoundException::new);
    }

    private Item getItem(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(ItemNotFoundException::new);
    }
}
