package br.app.coeur.modules.shopping.model;

import br.app.coeur.modules.shopping.exception.ItemNotInShoppingListException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShoppingListTest {

    private ShoppingList newShoppingList() {
        return ShoppingList.create("Mercado", 1L);
    }

    private Item newItem() {
        return Item.create("Arroz", "Arroz branco tipo 1", 20.0, null, "Tio João");
    }

    @Test
    void createShouldSetNameAndUserIdWithNoItems() {
        ShoppingList shoppingList = newShoppingList();

        assertThat(shoppingList.getName()).isEqualTo("Mercado");
        assertThat(shoppingList.getUserId()).isEqualTo(1L);
        assertThat(shoppingList.getItems()).isEmpty();
    }

    @Test
    void createShouldRejectBlankNameOrNullUserId() {
        assertThatThrownBy(() -> ShoppingList.create(" ", 1L))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Nome é obrigatório.");
        assertThatThrownBy(() -> ShoppingList.create("Mercado", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Usuário é obrigatório.");
    }

    @Test
    void renameShouldUpdateNameAndRejectBlankValue() {
        ShoppingList shoppingList = newShoppingList();

        shoppingList.rename("Feira");
        assertThat(shoppingList.getName()).isEqualTo("Feira");

        assertThatThrownBy(() -> shoppingList.rename(""))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Nome é obrigatório.");
        assertThat(shoppingList.getName()).isEqualTo("Feira");
    }

    @Test
    void addItemShouldCreateNewEntryWithGivenQuantity() {
        ShoppingList shoppingList = newShoppingList();
        Item item = newItem();

        ItemList itemList = shoppingList.addItem(item, 2);

        assertThat(shoppingList.getItems()).containsExactly(itemList);
        assertThat(itemList.getItem()).isEqualTo(item);
        assertThat(itemList.getQuantity()).isEqualTo(2);
    }

    @Test
    void addItemShouldIncreaseQuantityWhenItemAlreadyInList() {
        ShoppingList shoppingList = newShoppingList();
        Item item = newItem();

        shoppingList.addItem(item, 2);
        ItemList itemList = shoppingList.addItem(item, 3);

        assertThat(shoppingList.getItems()).hasSize(1);
        assertThat(itemList.getQuantity()).isEqualTo(5);
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
                .hasMessage("Item não encontrado na lista de compras.");
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
                .hasMessage("Item não encontrado na lista de compras.");
    }
}
