package br.app.coeur.modules.user.dto;

import br.app.coeur.modules.user.domain.User;

public record UserResponse(Long id, String email, String name, String roles) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoles()
        );
    }
}
