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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private ShoppingList existingShoppingList() {
        ShoppingList shoppingList = ShoppingList.create("Groceries", 1L);
        ReflectionTestUtils.setField(shoppingList, "id", 10L);
        return shoppingList;
    }

    private Item existingItem() {
        Item item = Item.create("Rice", "White rice, type 1", 20.0, null, "Ben's");
        ReflectionTestUtils.setField(item, "id", 100L);
        return item;
    }

    @Test
    void createShouldPersistShoppingListForUser() {
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingListResponse response = shoppingListService.create(1L, new CreateShoppingListRequest("Groceries"));

        assertThat(response.name()).isEqualTo("Groceries");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.items()).isEmpty();
    }

    @Test
    void findByIdShouldReturnShoppingListOwnedByUser() {
        when(shoppingListRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingShoppingList()));

        ShoppingListResponse response = shoppingListService.findById(1L, 10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Groceries");
    }

    @Test
    void findByIdShouldFailWhenShoppingListDoesNotExistOrBelongsToAnotherUser() {
        when(shoppingListRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingListService.findById(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Shopping list not found.");
    }

    @Test
    void findAllShouldMapAllShoppingListsOfUser() {
        Pageable pageable = PageRequest.of(0, 20);
        when(shoppingListRepository.findAllByUserId(1L, pageable)).thenReturn(new PageImpl<>(List.of(existingShoppingList())));

        Page<ShoppingListResponse> responses = shoppingListService.findAll(1L, pageable);

        assertThat(responses.getContent()).extracting(ShoppingListResponse::name).containsExactly("Groceries");
    }

    @Test
    void renameShouldUpdateShoppingListName() {
        when(shoppingListRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingShoppingList()));

        ShoppingListResponse response = shoppingListService.rename(1L, 10L, new UpdateShoppingListRequest("Market"));

        assertThat(response.name()).isEqualTo("Market");
    }

    @Test
    void deleteShouldRemoveExistingShoppingList() {
        ShoppingList shoppingList = existingShoppingList();
        when(shoppingListRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(shoppingList));

        shoppingListService.delete(1L, 10L);

        org.mockito.Mockito.verify(shoppingListRepository).delete(shoppingList);
    }

    @Test
    void addItemShouldAppendItemWithQuantity() {
        when(shoppingListRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingShoppingList()));
        when(itemRepository.findById(100L)).thenReturn(Optional.of(existingItem()));

        ShoppingListResponse response = shoppingListService.addItem(1L, 10L, new AddItemToShoppingListRequest(100L, 2));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(response.items().getFirst().item().id()).isEqualTo(100L);
    }

    @Test
    void addItemShouldFailWhenItemDoesNotExist() {
        when(shoppingListRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(existingShoppingList()));
        when(itemRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingListService.addItem(1L, 10L, new AddItemToShoppingListRequest(100L, 2)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Item not found.");
    }

    @Test
    void removeItemShouldDeleteExistingEntry() {
        ShoppingList shoppingList = existingShoppingList();
        Item item = existingItem();
        shoppingList.addItem(item, 2);

        when(shoppingListRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(shoppingList));
        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        ShoppingListResponse response = shoppingListService.removeItem(1L, 10L, 100L);

        assertThat(response.items()).isEmpty();
    }

    @Test
    void changeItemQuantityShouldUpdateExistingEntry() {
        ShoppingList shoppingList = existingShoppingList();
        Item item = existingItem();
        shoppingList.addItem(item, 2);

        when(shoppingListRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(shoppingList));
        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        ShoppingListResponse response = shoppingListService.changeItemQuantity(1L, 10L, 100L, new UpdateItemQuantityRequest(7));

        assertThat(response.items().getFirst().quantity()).isEqualTo(7);
    }
}
