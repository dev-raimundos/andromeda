package br.app.coeur.modules.user.service;

import br.app.coeur.shared.exception.BusinessException;
import br.app.coeur.shared.exception.ConflictException;
import br.app.coeur.shared.exception.ResourceNotFoundException;
import br.app.coeur.modules.user.domain.User;
import br.app.coeur.modules.user.dto.RegisterUserRequest;
import br.app.coeur.modules.user.dto.UpdateUserRequest;
import br.app.coeur.modules.user.dto.UserResponse;
import br.app.coeur.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException("Senha é obrigatória.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("E-mail já está em uso.");
        }

        User user = User.register(request.email(), passwordEncoder.encode(request.password()), request.name());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUser(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getUser(id);

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ConflictException("E-mail já está em uso.");
            }
            user.changeEmail(request.email());
        }

        if (request.name() != null) {
            user.rename(request.name());
        }

        if (request.roles() != null) {
            user.changeRoles(request.roles());
        }

        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(getUser(id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Usuário não encontrado.")
        );
    }
}
