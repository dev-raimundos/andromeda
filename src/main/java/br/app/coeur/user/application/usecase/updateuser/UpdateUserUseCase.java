package br.app.coeur.user.application.usecase.updateuser;

import br.app.coeur.user.application.abstraction.UserRepository;
import br.app.coeur.user.application.usecase.UserOutput;
import br.app.coeur.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    public UserOutput execute(Long id, UpdateUserInput input) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (input.getEmail() != null && !input.getEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(input.getEmail())) {
                throw new IllegalArgumentException("E-mail já está em uso.");
            }
            user.setEmail(input.getEmail());
        }

        if (input.getName() != null) {
            user.setName(input.getName());
        }

        if (input.getRoles() != null) {
            user.setRoles(input.getRoles());
        }

        User savedUser = userRepository.save(user);

        return UserOutput.from(savedUser);
    }
}
