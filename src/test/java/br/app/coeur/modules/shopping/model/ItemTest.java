package br.app.coeur.modules.shopping.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemTest {

    private Item newItem() {
        return Item.create("Arroz", "Arroz branco tipo 1", 20.0, "https://store.com/arroz", "Tio João");
    }

    @Test
    void createShouldSetAllFields() {
        Item item = newItem();

        assertThat(item.getName()).isEqualTo("Arroz");
        assertThat(item.getDescription()).isEqualTo("Arroz branco tipo 1");
        assertThat(item.getPrice()).isEqualTo(20.0);
        assertThat(item.getUrl()).isEqualTo("https://store.com/arroz");
        assertThat(item.getBrand()).isEqualTo("Tio João");
    }

    @Test
    void createShouldAllowNullUrl() {
        Item item = Item.create("Arroz", "Arroz branco tipo 1", 20.0, null, "Tio João");

        assertThat(item.getUrl()).isNull();
    }

    @Test
    void createShouldRejectBlankRequiredFields() {
        assertThatThrownBy(() -> Item.create(" ", "desc", 1.0, null, "brand"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Nome é obrigatório.");
        assertThatThrownBy(() -> Item.create("name", "", 1.0, null, "brand"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Descrição é obrigatória.");
        assertThatThrownBy(() -> Item.create("name", "desc", 1.0, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Marca é obrigatória.");
    }

    @Test
    void createShouldRejectNullOrNegativePrice() {
        assertThatThrownBy(() -> Item.create("name", "desc", null, null, "brand"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Preço não pode ser negativo.");
        assertThatThrownBy(() -> Item.create("name", "desc", -1.0, null, "brand"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Preço não pode ser negativo.");
    }

    @Test
    void createShouldAcceptZeroPrice() {
        Item item = Item.create("name", "desc", 0.0, null, "brand");

        assertThat(item.getPrice()).isZero();
    }

    @Test
    void changesShouldUpdateFields() {
        Item item = newItem();

        item.rename("Feijão");
        item.changeDescription("Feijão carioca");
        item.changePrice(15.0);
        item.changeUrl("https://store.com/feijao");
        item.changeBrand("Camil");

        assertThat(item.getName()).isEqualTo("Feijão");
        assertThat(item.getDescription()).isEqualTo("Feijão carioca");
        assertThat(item.getPrice()).isEqualTo(15.0);
        assertThat(item.getUrl()).isEqualTo("https://store.com/feijao");
        assertThat(item.getBrand()).isEqualTo("Camil");
    }

    @Test
    void changesShouldRejectInvalidValuesAndKeepPreviousState() {
        Item item = newItem();

        assertThatThrownBy(() -> item.rename(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item.changeDescription(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item.changePrice(-5.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> item.changeBrand("")).isInstanceOf(IllegalArgumentException.class);

        assertThat(item.getName()).isEqualTo("Arroz");
        assertThat(item.getDescription()).isEqualTo("Arroz branco tipo 1");
        assertThat(item.getPrice()).isEqualTo(20.0);
        assertThat(item.getBrand()).isEqualTo("Tio João");
    }
}
