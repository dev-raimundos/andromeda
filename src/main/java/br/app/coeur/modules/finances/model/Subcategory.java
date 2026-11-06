package br.app.coeur.modules.finances.model;

import br.app.coeur.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
@Table(name = "subcategory", schema = "finances")
public class Subcategory extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",  nullable = false)
    private Category category;

    @Column(name = "name",  nullable = false)
    private String name;

    @Column(name = "description")
    private String description;
}
