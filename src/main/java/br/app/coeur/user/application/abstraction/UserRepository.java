package br.app.coeur.user.application.abstraction;

import br.app.coeur.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAll();
    User save(User user);
    void deleteById(Long id);
}
