package br.app.coeur.authentication.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @Column("id")
    private Long id;

    @Column("token")
    private String token;

    @Column("user_id")
    private Long userId;

    @Column("expiry_date")
    private Instant expiryDate;

    @Column("revoked")
    private boolean revoked;
}
