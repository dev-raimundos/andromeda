package br.app.coeur.shopping.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShoppingListItemTest {

    private Item newItem() {
        return Item.create("Rice", "White rice, type 1", 20.0, null, "Ben's");
    }

    private ShoppingList newShoppingList() {
        return ShoppingList.create("Groceries", UUID.randomUUID());
    }

    private ShoppingListItem newShoppingListItem(int quantity) {
        return ShoppingListItem.of(newShoppingList(), newItem(), quantity);
    }

    @Test
    void ofShouldCreatePendingItemWithGivenQuantity() {
        ShoppingListItem shoppingListItem = newShoppingListItem(3);

        assertThat(shoppingListItem.getQuantity()).isEqualTo(3);
        assertThat(shoppingListItem.isPurchased()).isFalse();
    }

    @Test
    void ofShouldRejectNonPositiveQuantity() {
        assertThatThrownBy(() -> newShoppingListItem(0))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Quantity must be greater than zero.");
        assertThatThrownBy(() -> newShoppingListItem(-1))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Quantity must be greater than zero.");
    }

    @Test
    void increaseQuantityShouldAddAmount() {
        ShoppingListItem shoppingListItem = newShoppingListItem(2);

        shoppingListItem.increaseQuantity(3);

        assertThat(shoppingListItem.getQuantity()).isEqualTo(5);
    }

    @Test
    void increaseQuantityShouldRejectNonPositiveAmount() {
        ShoppingListItem shoppingListItem = newShoppingListItem(2);

        assertThatThrownBy(() -> shoppingListItem.increaseQuantity(0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(shoppingListItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void decreaseQuantityShouldSubtractAmount() {
        ShoppingListItem shoppingListItem = newShoppingListItem(5);

        shoppingListItem.decreaseQuantity(3);

        assertThat(shoppingListItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void decreaseQuantityShouldRejectResultNotGreaterThanZero() {
        ShoppingListItem shoppingListItem = newShoppingListItem(2);

        assertThatThrownBy(() -> shoppingListItem.decreaseQuantity(2))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Quantity must be greater than zero.");
        assertThat(shoppingListItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void changeQuantityShouldReplaceValue() {
        ShoppingListItem shoppingListItem = newShoppingListItem(1);

        shoppingListItem.changeQuantity(10);

        assertThat(shoppingListItem.getQuantity()).isEqualTo(10);
    }

    @Test
    void changeQuantityShouldRejectNonPositiveValue() {
        ShoppingListItem shoppingListItem = newShoppingListItem(1);

        assertThatThrownBy(() -> shoppingListItem.changeQuantity(0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(shoppingListItem.getQuantity()).isEqualTo(1);
    }

    @Test
    void markAsPurchasedAndMarkAsPendingShouldToggleStatus() {
        ShoppingListItem shoppingListItem = newShoppingListItem(1);

        shoppingListItem.markAsPurchased();
        assertThat(shoppingListItem.isPurchased()).isTrue();

        shoppingListItem.markAsPending();
        assertThat(shoppingListItem.isPurchased()).isFalse();
    }
}
