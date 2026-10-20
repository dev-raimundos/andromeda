package br.app.coeur.shopping.domain;

import br.app.coeur.shopping.domain.exception.ItemNotInShoppingListException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShoppingListTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private ShoppingList newShoppingList() {
        return ShoppingList.create("Groceries", USER_ID);
    }

    private Item newItem() {
        return Item.create("Rice", "White rice, type 1", 20.0, null, "Ben's");
    }

    @Test
    void createShouldSetNameAndUserIdWithNoItems() {
        ShoppingList shoppingList = newShoppingList();

        assertThat(shoppingList.getName()).isEqualTo("Groceries");
        assertThat(shoppingList.getUserId()).isEqualTo(USER_ID);
        assertThat(shoppingList.getItems()).isEmpty();
    }

    @Test
    void createShouldRejectBlankNameOrNullUserId() {
        assertThatThrownBy(() -> ShoppingList.create(" ", USER_ID))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Name is required.");
        assertThatThrownBy(() -> ShoppingList.create("Groceries", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("User is required.");
    }

    @Test
    void renameShouldUpdateNameAndRejectBlankValue() {
        ShoppingList shoppingList = newShoppingList();

        shoppingList.rename("Market");
        assertThat(shoppingList.getName()).isEqualTo("Market");

        assertThatThrownBy(() -> shoppingList.rename(""))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Name is required.");
        assertThat(shoppingList.getName()).isEqualTo("Market");
    }

    @Test
    void addItemShouldCreateNewEntryWithGivenQuantity() {
        ShoppingList shoppingList = newShoppingList();
        Item item = newItem();

        ShoppingListItem shoppingListItem = shoppingList.addItem(item, 2);

        assertThat(shoppingList.getItems()).containsExactly(shoppingListItem);
        assertThat(shoppingListItem.getItem()).isEqualTo(item);
        assertThat(shoppingListItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void addItemShouldIncreaseQuantityWhenItemAlreadyInList() {
        ShoppingList shoppingList = newShoppingList();
        Item item = newItem();

        shoppingList.addItem(item, 2);
        ShoppingListItem shoppingListItem = shoppingList.addItem(item, 3);

        assertThat(shoppingList.getItems()).hasSize(1);
        assertThat(shoppingListItem.getQuantity()).isEqualTo(5);
    }

    @Test
    void removeItemShouldDeleteExistingEntry() {
        ShoppingList shoppingList = newShoppingList();
        Item item = newItem();
        shoppingList.addItem(item, 2);

        shoppingList.removeItem(item);

        assertThat(shoppingList.getItems()).isEmpty();
    }

    @Test
    void removeItemShouldFailWhenItemIsNotInList() {
        ShoppingList shoppingList = newShoppingList();

        assertThatThrownBy(() -> shoppingList.removeItem(newItem()))
                .isInstanceOf(ItemNotInShoppingListException.class)
                .hasMessage("Item not found in the shopping list.");
    }

    @Test
    void changeItemQuantityShouldUpdateExistingEntry() {
        ShoppingList shoppingList = newShoppingList();
        Item item = newItem();
        shoppingList.addItem(item, 2);

        shoppingList.changeItemQuantity(item, 10);

        assertThat(shoppingList.getItems().getFirst().getQuantity()).isEqualTo(10);
    }

    @Test
    void changeItemQuantityShouldFailWhenItemIsNotInList() {
        ShoppingList shoppingList = newShoppingList();

        assertThatThrownBy(() -> shoppingList.changeItemQuantity(newItem(), 5))
                .isInstanceOf(ItemNotInShoppingListException.class)
                .hasMessage("Item not found in the shopping list.");
    }
}
