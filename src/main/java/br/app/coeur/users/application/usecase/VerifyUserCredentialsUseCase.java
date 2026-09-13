package br.app.coeur.users.application.usecase;

import br.app.coeur.users.application.dto.UserResponse;
import br.app.coeur.users.domain.PasswordHasher;
import br.app.coeur.users.domain.User;
import br.app.coeur.users.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VerifyUserCredentialsUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = IllegalArgumentException.class)
    public UserResponse execute(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        Instant now = Instant.now();
        if (user.isLocked(now)) {
            throw new IllegalArgumentException("Conta temporariamente bloqueada devido a múltiplas tentativas falhas. Tente novamente mais tarde.");
        }

        if (passwordHasher.matches(rawPassword, user.getPassword())) {
            user.resetFailedAttempts();
            userRepository.save(user);
            return mapToResponse(user);
        } else {
            user.incrementFailedAttempts(now);
            userRepository.save(user);
            if (user.isLocked(now)) {
                throw new IllegalArgumentException("Conta bloqueada temporariamente devido a múltiplas tentativas falhas.");
            }
            throw new IllegalArgumentException("Credenciais inválidas.");
        }
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles())
                .build();
    }
}
