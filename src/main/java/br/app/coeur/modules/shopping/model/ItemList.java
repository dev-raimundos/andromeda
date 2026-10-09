package br.app.coeur.modules.shopping.model;

import br.app.coeur.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shopping_list_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemList extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean purchased;

    private ItemList(ShoppingList shoppingList, Item item, int quantity) {
        this.shoppingList = shoppingList;
        this.item = item;
        this.quantity = requirePositive(quantity);
        this.purchased = false;
    }

    static ItemList of(ShoppingList shoppingList, Item item, int quantity) {
        return new ItemList(shoppingList, item, quantity);
    }

    public void increaseQuantity(int amount) {
        this.quantity = requirePositive(this.quantity + requirePositive(amount));
    }

    public void decreaseQuantity(int amount) {
        changeQuantity(this.quantity - requirePositive(amount));
    }

    public void changeQuantity(int quantity) {
        this.quantity = requirePositive(quantity);
    }

    public void markAsPurchased() {
        this.purchased = true;
    }

    public void markAsPending() {
        this.purchased = false;
    }

    private static int requirePositive(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        return value;
    }
}
