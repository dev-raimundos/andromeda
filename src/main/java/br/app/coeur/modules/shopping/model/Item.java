package br.app.coeur.modules.shopping.model;

import br.app.coeur.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    private String url;

    @Column(nullable = false)
    private String brand;

    private Item(String name, String description, Double price, String url, String brand) {
        this.name = requireText(name, "Nome é obrigatório.");
        this.description = requireText(description, "Descrição é obrigatória.");
        this.price = requirePositive(price);
        this.url = url;
        this.brand = requireText(brand, "Marca é obrigatória.");
    }

    public static Item create(String name, String description, Double price, String url, String brand) {
        return new Item(name, description, price, url, brand);
    }

    public void rename(String name) {
        this.name = requireText(name, "Nome é obrigatório.");
    }

    public void changeDescription(String description) {
        this.description = requireText(description, "Descrição é obrigatória.");
    }

    public void changePrice(Double price) {
        this.price = requirePositive(price);
    }

    public void changeUrl(String url) {
        this.url = url;
    }

    public void changeBrand(String brand) {
        this.brand = requireText(brand, "Marca é obrigatória.");
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static Double requirePositive(Double value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo.");
        }
        return value;
    }
}
