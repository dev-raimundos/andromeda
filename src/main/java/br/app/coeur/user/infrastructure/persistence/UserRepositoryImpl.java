package br.app.coeur.user.infrastructure.persistence;

import br.app.coeur.user.application.abstraction.UserRepository;
import br.app.coeur.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJdbcRepository jdbcRepository;

    @Override
    public Optional<User> findById(Long id) {
        return jdbcRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbcRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jdbcRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return jdbcRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public User save(User user) {
        return toDomain(jdbcRepository.save(toEntity(user)));
    }

    @Override
    public void deleteById(Long id) {
        jdbcRepository.deleteById(id);
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

    private UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .roles(user.getRoles())
                .failedAttempts(user.getFailedAttempts())
                .lockExpiredAt(user.getLockExpiredAt())
                .build();
    }
}
