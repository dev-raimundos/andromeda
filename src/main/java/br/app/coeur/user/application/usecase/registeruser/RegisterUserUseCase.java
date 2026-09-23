package br.app.coeur.user.application.usecase.registeruser;

import br.app.coeur.user.application.abstraction.UserRepository;
import br.app.coeur.user.application.usecase.UserOutput;
import br.app.coeur.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserOutput execute(RegisterUserInput input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }

        User user = User.builder()
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .name(input.getName())
                .roles("ROLE_USER")
                .failedAttempts(0)
                .build();

        User savedUser = userRepository.save(user);
        return UserOutput.from(savedUser);
    }
}
