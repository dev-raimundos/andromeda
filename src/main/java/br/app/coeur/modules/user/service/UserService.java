package br.app.coeur.modules.user.service;

import br.app.coeur.modules.user.domain.User;
import br.app.coeur.modules.user.dto.RegisterUserRequest;
import br.app.coeur.modules.user.dto.UpdateUserRequest;
import br.app.coeur.modules.user.dto.UserResponse;
import br.app.coeur.modules.user.exception.EmailAlreadyInUseException;
import br.app.coeur.modules.user.exception.UserNotFoundException;
import br.app.coeur.modules.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor()
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterUserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException();
        }

        User user = User.register(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );

        return UserResponse.from(
                userRepository.save(user)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUser(id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getUser(id);

        if (!request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new EmailAlreadyInUseException();
            }
            user.changeEmail(request.email());
        }
        user.rename(request.name());

        user.changeRoles(request.roles());

        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.delete(
                getUser(id)
        );
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }
}
