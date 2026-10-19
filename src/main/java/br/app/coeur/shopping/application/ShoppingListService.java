package br.app.coeur.shopping.application;

import br.app.coeur.shopping.application.dto.request.AddItemToShoppingListRequest;
import br.app.coeur.shopping.application.dto.request.CreateShoppingListRequest;
import br.app.coeur.shopping.application.dto.request.UpdateItemQuantityRequest;
import br.app.coeur.shopping.application.dto.request.UpdateShoppingListRequest;
import br.app.coeur.shopping.application.dto.response.ShoppingListResponse;
import br.app.coeur.shopping.domain.Item;
import br.app.coeur.shopping.domain.ItemRepository;
import br.app.coeur.shopping.domain.ShoppingList;
import br.app.coeur.shopping.domain.ShoppingListRepository;
import br.app.coeur.shopping.domain.exception.ItemNotFoundException;
import br.app.coeur.shopping.domain.exception.ShoppingListNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public ShoppingListResponse create(Long userId, CreateShoppingListRequest request) {
        ShoppingList shoppingList = ShoppingList.create(request.name(), userId);

        return ShoppingListResponse.from(
                shoppingListRepository.save(shoppingList)
        );
    }

    @Cacheable(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional(readOnly = true)
    public ShoppingListResponse findById(Long userId, Long id) {
        return ShoppingListResponse.from(getShoppingList(userId, id));
    }

    @Transactional(readOnly = true)
    public Page<ShoppingListResponse> findAll(Long userId, Pageable pageable) {
        return shoppingListRepository.findAllByUserId(userId, pageable)
                .map(ShoppingListResponse::from);
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public ShoppingListResponse rename(Long userId, Long id, UpdateShoppingListRequest request) {
        ShoppingList shoppingList = getShoppingList(userId, id);
        shoppingList.rename(request.name());
        return ShoppingListResponse.from(shoppingList);
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public void delete(Long userId, Long id) {
        shoppingListRepository.delete(
                getShoppingList(userId, id)
        );
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public ShoppingListResponse addItem(Long userId, Long id, AddItemToShoppingListRequest request) {
        ShoppingList shoppingList = getShoppingList(userId, id);
        Item item = getItem(request.itemId());

        shoppingList.addItem(item, request.quantity());

        return ShoppingListResponse.from(shoppingList);
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public ShoppingListResponse removeItem(Long userId, Long id, Long itemId) {
        ShoppingList shoppingList = getShoppingList(userId, id);
        Item item = getItem(itemId);

        shoppingList.removeItem(item);

        return ShoppingListResponse.from(shoppingList);
    }

    @CacheEvict(cacheNames = ShoppingCacheNames.SHOPPING_LISTS, key = "#userId + ':' + #id")
    @Transactional
    public ShoppingListResponse changeItemQuantity(Long userId, Long id, Long itemId, UpdateItemQuantityRequest request) {
        ShoppingList shoppingList = getShoppingList(userId, id);
        Item item = getItem(itemId);

        shoppingList.changeItemQuantity(item, request.quantity());

        return ShoppingListResponse.from(shoppingList);
    }

    private ShoppingList getShoppingList(Long userId, Long id) {
        return shoppingListRepository.findByIdAndUserId(id, userId)
                .orElseThrow(ShoppingListNotFoundException::new);
    }

    private Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(ItemNotFoundException::new);
    }
}
