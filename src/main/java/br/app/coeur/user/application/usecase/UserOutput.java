package br.app.coeur.user.application.usecase;

import br.app.coeur.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOutput {
    private Long id;
    private String email;
    private String name;
    private String roles;

    public static UserOutput from(User user) {
        return UserOutput.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles())
                .build();
    }
}
