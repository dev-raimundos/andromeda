package br.app.coeur.modules.shopping.model;

import br.app.coeur.modules.shopping.exception.ItemNotInShoppingListException;
import br.app.coeur.shared.persistence.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Entity
@Table(name = "shopping_lists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingList extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ItemList> items = new ArrayList<>();

    private ShoppingList(String name, Long userId) {
        this.name = requireText(name, "Nome é obrigatório.");
        this.userId = requireNonNull(userId, "Usuário é obrigatório.");
    }

    public static ShoppingList create(String name, Long userId) {
        return new ShoppingList(name, userId);
    }

    public void rename(String name) {
        this.name = requireText(name, "Nome é obrigatório.");
    }

    public ItemList addItem(Item item, int quantity) {

        Optional<ItemList> existing = findItemOf(item);

        if (existing.isPresent()) {
            existing.get().increaseQuantity(quantity);
            return existing.get();
        }

        ItemList itemList = ItemList.of(this, item, quantity);
        this.items.add(itemList);
        return itemList;
    }

    public void removeItem(Item item) {
        ItemList itemList = findItemOf(item).orElseThrow(ItemNotInShoppingListException::new);
        this.items.remove(itemList);
    }

    public void changeItemQuantity(Item item, int quantity) {
        ItemList itemList = findItemOf(item).orElseThrow(ItemNotInShoppingListException::new);
        itemList.changeQuantity(quantity);
    }

    private Optional<ItemList> findItemOf(Item item) {
        return this.items.stream()
                .filter(itemList -> itemList.getItem().equals(item))
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
