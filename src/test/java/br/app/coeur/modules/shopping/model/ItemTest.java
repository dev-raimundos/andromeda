package br.app.coeur.modules.shopping.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemTest {

    private Item newItem() {
        return Item.create("Rice", "White rice, type 1", 20.0, "https://store.com/rice", "Ben's");
    }

    @Test
    void createShouldSetAllFields() {
        Item item = newItem();

        assertThat(item.getName()).isEqualTo("Rice");
        assertThat(item.getDescription()).isEqualTo("White rice, type 1");
        assertThat(item.getPrice()).isEqualTo(20.0);
        assertThat(item.getUrl()).isEqualTo("https://store.com/rice");
        assertThat(item.getBrand()).isEqualTo("Ben's");
    }

    @Test
    void createShouldAllowNullUrl() {
        Item item = Item.create("Rice", "White rice, type 1", 20.0, null, "Ben's");

        assertThat(item.getUrl()).isNull();
    }

    @Test
    void createShouldRejectBlankRequiredFields() {
        assertThatThrownBy(() -> Item.create(" ", "desc", 1.0, null, "brand"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Name is required.");
        assertThatThrownBy(() -> Item.create("name", "", 1.0, null, "brand"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Description is required.");
        assertThatThrownBy(() -> Item.create("name", "desc", 1.0, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Brand is required.");
    }

    @Test
    void createShouldRejectNullOrNegativePrice() {
        assertThatThrownBy(() -> Item.create("name", "desc", null, null, "brand"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Price cannot be negative.");
        assertThatThrownBy(() -> Item.create("name", "desc", -1.0, null, "brand"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Price cannot be negative.");
    }

    @Test
    void createShouldAcceptZeroPrice() {
        Item item = Item.create("name", "desc", 0.0, null, "brand");

        assertThat(item.getPrice()).isZero();
    }

    @Test
    void changesShouldUpdateFields() {
        Item item = newItem();

        item.rename("Beans");
        item.changeDescription("Carioca beans");
        item.changePrice(15.0);
        item.changeUrl("https://store.com/beans");
        item.changeBrand("Acme");

        assertThat(item.getName()).isEqualTo("Beans");
        assertThat(item.getDescription()).isEqualTo("Carioca beans");
        assertThat(item.getPrice()).isEqualTo(15.0);
        assertThat(item.getUrl()).isEqualTo("https://store.com/beans");
        assertThat(item.getBrand()).isEqualTo("Acme");
    }

    @Test
    void changesShouldRejectInvalidValuesAndKeepPreviousState() {
        Item item = newItem();

        assertThatThrownBy(() -> item.rename(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item.changeDescription(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item.changePrice(-5.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item.changeBrand("")).isInstanceOf(IllegalArgumentException.class);

        assertThat(item.getName()).isEqualTo("Rice");
        assertThat(item.getDescription()).isEqualTo("White rice, type 1");
        assertThat(item.getPrice()).isEqualTo(20.0);
        assertThat(item.getBrand()).isEqualTo("Ben's");
    }
}
