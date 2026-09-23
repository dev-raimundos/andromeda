package br.app.coeur.user.infrastructure.persistence;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

interface UserJdbcRepository extends ListCrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
