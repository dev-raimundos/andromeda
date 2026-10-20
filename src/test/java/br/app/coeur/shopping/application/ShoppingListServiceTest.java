package br.app.coeur.shopping.application;

import br.app.coeur.shared.exception.ResourceNotFoundException;
import br.app.coeur.shopping.application.dto.request.AddItemToShoppingListRequest;
import br.app.coeur.shopping.application.dto.request.CreateShoppingListRequest;
import br.app.coeur.shopping.application.dto.request.UpdateItemQuantityRequest;
import br.app.coeur.shopping.application.dto.request.UpdateShoppingListRequest;
import br.app.coeur.shopping.application.dto.response.ShoppingListResponse;
import br.app.coeur.shopping.domain.Item;
import br.app.coeur.shopping.domain.ItemRepository;
import br.app.coeur.shopping.domain.ShoppingList;
import br.app.coeur.shopping.domain.ShoppingListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SHOPPING_LIST_ID = UUID.randomUUID();
    private static final UUID ITEM_ID = UUID.randomUUID();

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private ShoppingList existingShoppingList() {
        ShoppingList shoppingList = ShoppingList.create("Groceries", USER_ID);
        ReflectionTestUtils.setField(shoppingList, "id", SHOPPING_LIST_ID);
        return shoppingList;
    }

    private Item existingItem() {
        Item item = Item.create("Rice", "White rice, type 1", 20.0, null, "Ben's");
        ReflectionTestUtils.setField(item, "id", ITEM_ID);
        return item;
    }

    @Test
    void createShouldPersistShoppingListForUser() {
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingListResponse response = shoppingListService.create(USER_ID, new CreateShoppingListRequest("Groceries"));

        assertThat(response.name()).isEqualTo("Groceries");
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.items()).isEmpty();
    }

    @Test
    void findByIdShouldReturnShoppingListOwnedByUser() {
        when(shoppingListRepository.findByIdAndUserId(SHOPPING_LIST_ID, USER_ID)).thenReturn(Optional.of(existingShoppingList()));

        ShoppingListResponse response = shoppingListService.findById(USER_ID, SHOPPING_LIST_ID);

        assertThat(response.id()).isEqualTo(SHOPPING_LIST_ID);
        assertThat(response.name()).isEqualTo("Groceries");
    }

    @Test
    void findByIdShouldFailWhenShoppingListDoesNotExistOrBelongsToAnotherUser() {
        when(shoppingListRepository.findByIdAndUserId(SHOPPING_LIST_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingListService.findById(USER_ID, SHOPPING_LIST_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Shopping list not found.");
    }

    @Test
    void findAllShouldMapAllShoppingListsOfUser() {
        Pageable pageable = PageRequest.of(0, 20);
        when(shoppingListRepository.findAllByUserId(USER_ID, pageable)).thenReturn(new PageImpl<>(List.of(existingShoppingList())));

        Page<ShoppingListResponse> responses = shoppingListService.findAll(USER_ID, pageable);

        assertThat(responses.getContent()).extracting(ShoppingListResponse::name).containsExactly("Groceries");
    }

    @Test
    void renameShouldUpdateShoppingListName() {
        when(shoppingListRepository.findByIdAndUserId(SHOPPING_LIST_ID, USER_ID)).thenReturn(Optional.of(existingShoppingList()));

        ShoppingListResponse response = shoppingListService.rename(USER_ID, SHOPPING_LIST_ID, new UpdateShoppingListRequest("Market"));

        assertThat(response.name()).isEqualTo("Market");
    }

    @Test
    void deleteShouldRemoveExistingShoppingList() {
        ShoppingList shoppingList = existingShoppingList();
        when(shoppingListRepository.findByIdAndUserId(SHOPPING_LIST_ID, USER_ID)).thenReturn(Optional.of(shoppingList));

        shoppingListService.delete(USER_ID, SHOPPING_LIST_ID);

        verify(shoppingListRepository).delete(shoppingList);
    }

    @Test
    void addItemShouldAppendItemWithQuantity() {
        when(shoppingListRepository.findByIdAndUserId(SHOPPING_LIST_ID, USER_ID)).thenReturn(Optional.of(existingShoppingList()));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(existingItem()));

        ShoppingListResponse response = shoppingListService.addItem(USER_ID, SHOPPING_LIST_ID, new AddItemToShoppingListRequest(ITEM_ID, 2));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(response.items().getFirst().item().id()).isEqualTo(ITEM_ID);
    }

    @Test
    void addItemShouldFailWhenItemDoesNotExist() {
        when(shoppingListRepository.findByIdAndUserId(SHOPPING_LIST_ID, USER_ID)).thenReturn(Optional.of(existingShoppingList()));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());
        AddItemToShoppingListRequest request = new AddItemToShoppingListRequest(ITEM_ID, 2);

        assertThatThrownBy(() -> shoppingListService.addItem(USER_ID, SHOPPING_LIST_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found.");
    }

    @Test
    void removeItemShouldDeleteExistingEntry() {
        ShoppingList shoppingList = existingShoppingList();
        Item item = existingItem();
        shoppingList.addItem(item, 2);

        when(shoppingListRepository.findByIdAndUserId(SHOPPING_LIST_ID, USER_ID)).thenReturn(Optional.of(shoppingList));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        ShoppingListResponse response = shoppingListService.removeItem(USER_ID, SHOPPING_LIST_ID, ITEM_ID);

        assertThat(response.items()).isEmpty();
    }

    @Test
    void changeItemQuantityShouldUpdateExistingEntry() {
        ShoppingList shoppingList = existingShoppingList();
        Item item = existingItem();
        shoppingList.addItem(item, 2);

        when(shoppingListRepository.findByIdAndUserId(SHOPPING_LIST_ID, USER_ID)).thenReturn(Optional.of(shoppingList));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        ShoppingListResponse response = shoppingListService.changeItemQuantity(USER_ID, SHOPPING_LIST_ID, ITEM_ID, new UpdateItemQuantityRequest(7));

        assertThat(response.items().getFirst().quantity()).isEqualTo(7);
    }
}
