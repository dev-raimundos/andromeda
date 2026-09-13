package br.app.coeur.users.application;

import br.app.coeur.users.application.dto.UserRegisterRequest;
import br.app.coeur.users.application.dto.UserResponse;
import br.app.coeur.users.application.dto.UserUpdateRequest;
import br.app.coeur.users.application.usecase.DeleteUserUseCase;
import br.app.coeur.users.application.usecase.FindUserByIdUseCase;
import br.app.coeur.users.application.usecase.ListUsersUseCase;
import br.app.coeur.users.application.usecase.RegisterUserUseCase;
import br.app.coeur.users.application.usecase.UpdateUserUseCase;
import br.app.coeur.users.application.usecase.VerifyUserCredentialsUseCase;
import br.app.coeur.users.domain.abstractions.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAppService {

    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyUserCredentialsUseCase verifyUserCredentialsUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final UserRepository userRepository;

    public UserResponse register(UserRegisterRequest request) {
        return registerUserUseCase.execute(request);
    }

    public Optional<UserResponse> authenticate(String email, String rawPassword) {
        try {
            return Optional.of(verifyUserCredentialsUseCase.execute(email, rawPassword));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("bloquead") || e.getMessage().contains("múltiplas tentativas")) {
                throw e;
            }
            return Optional.empty();
        }
    }

    public Optional<UserResponse> findById(Long id) {
        try {
            return Optional.of(findUserByIdUseCase.execute(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email).map(user -> UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles())
                .build());
    }

    public UserResponse update(Long id, UserUpdateRequest request) {
        return updateUserUseCase.execute(id, request);
    }

    public void delete(Long id) {
        deleteUserUseCase.execute(id);
    }

    public List<UserResponse> listAll() {
        return listUsersUseCase.execute();
    }
}
