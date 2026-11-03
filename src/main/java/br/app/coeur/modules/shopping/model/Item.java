package br.app.coeur.modules.shopping.model;

import br.app.coeur.shared.model.BaseEntity;
import br.app.coeur.shared.util.ValidationUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Entity
@Table(name = "items")
public class Item extends BaseEntity<UUID> {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    private String url;

    @Column(nullable = false)
    private String brand;

    protected Item() {
    }

    private Item(String name, String description, Double price, String url, String brand) {
        this.name = ValidationUtils.requireText(name, "Name is required.");
        this.description = ValidationUtils.requireText(description, "Description is required.");
        this.price = requirePositive(price);
        this.url = url;
        this.brand = ValidationUtils.requireText(brand, "Brand is required.");
    }

    public static Item create(String name, String description, Double price, String url, String brand) {
        return new Item(name, description, price, url, brand);
    }

    public void rename(String name) {
        this.name = ValidationUtils.requireText(name, "Name is required.");
    }

    public void changeDescription(String description) {
        this.description = ValidationUtils.requireText(description, "Description is required.");
    }

    public void changePrice(Double price) {
        this.price = requirePositive(price);
    }

    public void changeUrl(String url) {
        this.url = url;
    }

    public void changeBrand(String brand) {
        this.brand = ValidationUtils.requireText(brand, "Brand is required.");
    }

    private static Double requirePositive(Double value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        return value;
    }
}
