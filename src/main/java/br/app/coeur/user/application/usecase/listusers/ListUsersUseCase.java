package br.app.coeur.user.application.usecase.listusers;

import br.app.coeur.user.application.abstraction.UserRepository;
import br.app.coeur.user.application.usecase.UserOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserOutput> execute() {
        return userRepository.findAll().stream()
                .map(UserOutput::from)
                .toList();
    }
}
