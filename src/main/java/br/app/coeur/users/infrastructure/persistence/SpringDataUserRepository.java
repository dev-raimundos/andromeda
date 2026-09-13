package br.app.coeur.users.infrastructure.persistence;

import org.springframework.data.repository.ListCrudRepository;
import java.util.Optional;

public interface SpringDataUserRepository extends ListCrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
