package br.app.coeur.users.application.usecase;

import br.app.coeur.users.application.dto.UserRegisterRequest;
import br.app.coeur.users.application.dto.UserResponse;
import br.app.coeur.users.domain.User;
import br.app.coeur.users.domain.abstractions.PasswordHasher;
import br.app.coeur.users.domain.abstractions.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional
    public UserResponse execute(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordHasher.hash(request.getPassword()))
                .name(request.getName())
                .roles("ROLE_USER")
                .failedAttempts(0)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
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
