package br.app.coeur.user.service;

import br.app.coeur.user.domain.User;
import br.app.coeur.user.dto.RegisterUserRequest;
import br.app.coeur.user.dto.UpdateUserRequest;
import br.app.coeur.user.dto.UserResponse;
import br.app.coeur.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User existingUser() {
        User user = User.register("john@coeur.app", "encoded", "John");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void registerShouldEncodePasswordAndPersistUserWithDefaultRole() {
        when(userRepository.existsByEmail("john@coeur.app")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.register(new RegisterUserRequest("john@coeur.app", "secret", "John"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(response.email()).isEqualTo("john@coeur.app");
        assertThat(response.name()).isEqualTo("John");
        assertThat(response.roles()).isEqualTo(User.DEFAULT_ROLE);
    }

    @Test
    void registerShouldFailWhenEmailAlreadyInUse() {
        when(userRepository.existsByEmail("john@coeur.app")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(new RegisterUserRequest("john@coeur.app", "secret", "John")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail já está em uso.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldFailWhenPasswordIsBlank() {
        assertThatThrownBy(() -> userService.register(new RegisterUserRequest("john@coeur.app", " ", "John")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Senha é obrigatória.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void findByIdShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));

        UserResponse response = userService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("john@coeur.app");
    }

    @Test
    void findByIdShouldFailWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuário não encontrado.");
    }

    @Test
    void findAllShouldMapAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                existingUser(), User.register("mary@coeur.app", "encoded", "Mary")));

        List<UserResponse> responses = userService.findAll();

        assertThat(responses).extracting(UserResponse::email)
                .containsExactly("john@coeur.app", "mary@coeur.app");
    }

    @Test
    void updateShouldChangeEmailNameAndRoles() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@coeur.app")).thenReturn(false);

        UserResponse response = userService.update(1L, new UpdateUserRequest("new@coeur.app", "New Name", "ROLE_ADMIN"));

        assertThat(response.email()).isEqualTo("new@coeur.app");
        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.roles()).isEqualTo("ROLE_ADMIN");
        assertThat(user.getEmail()).isEqualTo("new@coeur.app");
    }

    @Test
    void updateShouldIgnoreNullFields() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.update(1L, new UpdateUserRequest(null, null, null));

        assertThat(response.email()).isEqualTo("john@coeur.app");
        assertThat(response.name()).isEqualTo("John");
        assertThat(response.roles()).isEqualTo(User.DEFAULT_ROLE);
    }

    @Test
    void updateShouldNotCheckEmailUniquenessWhenEmailIsUnchanged() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));

        userService.update(1L, new UpdateUserRequest("john@coeur.app", "John Updated", null));

        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateShouldFailWhenNewEmailIsAlreadyInUse() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@coeur.app")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, new UpdateUserRequest("taken@coeur.app", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail já está em uso.");

        assertThat(user.getEmail()).isEqualTo("john@coeur.app");
    }

    @Test
    void updateShouldFailWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, new UpdateUserRequest("a@b.c", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuário não encontrado.");
    }

    @Test
    void deleteShouldRemoveExistingUser() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteShouldFailWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuário não encontrado.");

        verify(userRepository, never()).delete(any());
    }
}
