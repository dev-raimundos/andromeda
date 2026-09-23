package br.app.coeur.user.application.usecase.deleteuser;

import br.app.coeur.user.application.abstraction.UserRepository;
import br.app.coeur.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    public void execute(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        userRepository.deleteById(user.getId());
    }
}
