package br.app.coeur.users.usecase;

import br.app.coeur.users.dto.UserResponse;
import br.app.coeur.users.model.User;
import br.app.coeur.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> execute() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
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
