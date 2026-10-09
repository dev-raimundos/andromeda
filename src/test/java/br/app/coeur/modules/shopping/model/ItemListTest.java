package br.app.coeur.modules.shopping.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemListTest {

    private Item newItem() {
        return Item.create("Arroz", "Arroz branco tipo 1", 20.0, null, "Tio João");
    }

    private ShoppingList newShoppingList() {
        return ShoppingList.create("Mercado", 1L);
    }

    private ItemList newItemList(int quantity) {
        return ItemList.of(newShoppingList(), newItem(), quantity);
    }

    @Test
    void ofShouldCreatePendingItemListWithGivenQuantity() {
        ItemList itemList = newItemList(3);

        assertThat(itemList.getQuantity()).isEqualTo(3);
        assertThat(itemList.isPurchased()).isFalse();
    }

    @Test
    void ofShouldRejectNonPositiveQuantity() {
        assertThatThrownBy(() -> newItemList(0))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Quantidade deve ser maior que zero.");
        assertThatThrownBy(() -> newItemList(-1))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Quantidade deve ser maior que zero.");
    }

    @Test
    void increaseQuantityShouldAddAmount() {
        ItemList itemList = newItemList(2);

        itemList.increaseQuantity(3);

        assertThat(itemList.getQuantity()).isEqualTo(5);
    }

    @Test
    void increaseQuantityShouldRejectNonPositiveAmount() {
        ItemList itemList = newItemList(2);

        assertThatThrownBy(() -> itemList.increaseQuantity(0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(itemList.getQuantity()).isEqualTo(2);
    }

    @Test
    void decreaseQuantityShouldSubtractAmount() {
        ItemList itemList = newItemList(5);

        itemList.decreaseQuantity(3);

        assertThat(itemList.getQuantity()).isEqualTo(2);
    }

    @Test
    void decreaseQuantityShouldRejectResultNotGreaterThanZero() {
        ItemList itemList = newItemList(2);

        assertThatThrownBy(() -> itemList.decreaseQuantity(2))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Quantidade deve ser maior que zero.");
        assertThat(itemList.getQuantity()).isEqualTo(2);
    }

    @Test
    void changeQuantityShouldReplaceValue() {
        ItemList itemList = newItemList(1);

        itemList.changeQuantity(10);

        assertThat(itemList.getQuantity()).isEqualTo(10);
    }

    @Test
    void changeQuantityShouldRejectNonPositiveValue() {
        ItemList itemList = newItemList(1);

        assertThatThrownBy(() -> itemList.changeQuantity(0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(itemList.getQuantity()).isEqualTo(1);
    }

    @Test
    void markAsPurchasedAndMarkAsPendingShouldToggleStatus() {
        ItemList itemList = newItemList(1);

        itemList.markAsPurchased();
        assertThat(itemList.isPurchased()).isTrue();

        itemList.markAsPending();
        assertThat(itemList.isPurchased()).isFalse();
    }
}
