package br.app.coeur.user.application.usecase.finduserbyid;

import br.app.coeur.user.application.abstraction.UserRepository;
import br.app.coeur.user.application.usecase.UserOutput;
import br.app.coeur.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindUserByIdUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserOutput execute(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return UserOutput.from(user);
    }
}
