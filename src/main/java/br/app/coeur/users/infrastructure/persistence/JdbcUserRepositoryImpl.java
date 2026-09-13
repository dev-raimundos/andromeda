package br.app.coeur.users.infrastructure.persistence;

import br.app.coeur.users.domain.User;
import br.app.coeur.users.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcUserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository springRepository;

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .roles(user.getRoles())
                .failedAttempts(user.getFailedAttempts())
                .lockExpiredAt(user.getLockExpiredAt())
                .build();

        UserEntity savedEntity = springRepository.save(entity);
        user.setId(savedEntity.getId());
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return springRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return springRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        springRepository.deleteById(id);
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .name(entity.getName())
                .roles(entity.getRoles())
                .failedAttempts(entity.getFailedAttempts())
                .lockExpiredAt(entity.getLockExpiredAt())
                .build();
    }
}
