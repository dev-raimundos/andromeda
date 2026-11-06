package br.app.coeur.modules.finances.model;

import br.app.coeur.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Table(name = "list", schema = "finances")
public class List extends BaseEntity<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "reference_month", nullable = false)
    private Date referenceMonth;
}
