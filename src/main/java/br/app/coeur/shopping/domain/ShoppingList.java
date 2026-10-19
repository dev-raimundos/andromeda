package br.app.coeur.shopping.domain;

import br.app.coeur.shared.persistence.BaseEntity;
import br.app.coeur.shopping.domain.exception.ItemNotInShoppingListException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Entity
@Table(name = "shopping_lists")
public class ShoppingList extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ShoppingListItem> items = new ArrayList<>();

    protected ShoppingList() {
    }

    private ShoppingList(String name, Long userId) {
        this.name = requireText(name, "Name is required.");
        this.userId = requireNonNull(userId, "User is required.");
    }

    public static ShoppingList create(String name, Long userId) {
        return new ShoppingList(name, userId);
    }

    public void rename(String name) {
        this.name = requireText(name, "Name is required.");
    }

    public ShoppingListItem addItem(Item item, int quantity) {

        Optional<ShoppingListItem> existing = findItemOf(item);

        if (existing.isPresent()) {
            existing.get().increaseQuantity(quantity);
            return existing.get();
        }

        ShoppingListItem shoppingListItem = ShoppingListItem.of(this, item, quantity);
        this.items.add(shoppingListItem);
        return shoppingListItem;
    }

    public void removeItem(Item item) {
        ShoppingListItem shoppingListItem = findItemOf(item).orElseThrow(ItemNotInShoppingListException::new);
        this.items.remove(shoppingListItem);
    }

    public void changeItemQuantity(Item item, int quantity) {
        ShoppingListItem shoppingListItem = findItemOf(item).orElseThrow(ItemNotInShoppingListException::new);
        shoppingListItem.changeQuantity(quantity);
    }

    private Optional<ShoppingListItem> findItemOf(Item item) {
        return this.items.stream()
                .filter(shoppingListItem -> shoppingListItem.getItem().equals(item))
                .findFirst();
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static Long requireNonNull(Long value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
