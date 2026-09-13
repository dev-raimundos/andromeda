package br.app.coeur.users.application.usecase;

import br.app.coeur.users.application.dto.UserResponse;
import br.app.coeur.users.domain.User;
import br.app.coeur.users.domain.abstractions.PasswordHasher;
import br.app.coeur.users.domain.abstractions.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyUserCredentialsUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = IllegalArgumentException.class)
    public UserResponse execute(String email, String rawPassword) {
        log.info("[LOGIN] Tentativa de login iniciada para o e-mail: '{}'", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[LOGIN] Falha no login: e-mail '{}' não está cadastrado no sistema", email);
                    return new IllegalArgumentException("Credenciais inválidas.");
                });

        Instant now = Instant.now();
        if (user.isLocked(now)) {
            log.warn("[LOGIN] Falha no login: conta do usuário '{}' está bloqueada temporariamente até {}", email, user.getLockExpiredAt());
            throw new IllegalArgumentException("Conta temporariamente bloqueada devido a múltiplas tentativas falhas. Tente novamente mais tarde.");
        }

        if (passwordHasher.matches(rawPassword, user.getPassword())) {
            log.info("[LOGIN] Login bem-sucedido para o usuário: '{}' (ID: {})", email, user.getId());
            user.resetFailedAttempts();
            userRepository.save(user);
            return mapToResponse(user);
        } else {
            user.incrementFailedAttempts(now);
            userRepository.save(user);

            if (user.isLocked(now)) {
                log.error("[LOGIN] Falha no login: senha incorreta para o e-mail '{}'. Limite de tentativas atingido! Conta BLOQUEADA temporariamente até {}", email, user.getLockExpiredAt());
                throw new IllegalArgumentException("Conta bloqueada temporariamente devido a múltiplas tentativas falhas.");
            }

            log.warn("[LOGIN] Falha no login: senha incorreta para o e-mail '{}'. Tentativas falhas consecutivas: {}/5", email, user.getFailedAttempts());
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
