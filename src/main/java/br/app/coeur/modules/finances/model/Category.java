package br.app.coeur.modules.finances.model;

import br.app.coeur.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
@Table(name = "category", schema = "finances")
public class Category extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id",  nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scope_id",  nullable = false)
    private Scope scope;

    @Column(name = "name",  nullable = false)
    private String name;
}
