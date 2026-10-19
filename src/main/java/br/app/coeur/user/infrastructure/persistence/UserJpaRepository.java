package br.app.coeur.user.infrastructure.persistence;

import br.app.coeur.user.domain.User;
import br.app.coeur.user.domain.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends UserRepository, JpaRepository<User, Long> {
}
