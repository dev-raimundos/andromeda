package br.app.coeur.modules.finances.model;

import br.app.coeur.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Table(name = "entry", schema = "finances")
public class Entry extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", nullable = false)
    private Subcategory subcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private List list;

    @Column(name = "paid_at", nullable = true)
    private Date paidAt;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "due_date", nullable = true)
    private Date dueDate;
}
