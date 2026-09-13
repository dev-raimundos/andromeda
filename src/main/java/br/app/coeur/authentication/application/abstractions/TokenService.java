package br.app.coeur.authentication.application.abstractions;

import br.app.coeur.users.application.dto.UserResponse;

public interface TokenService {
    String generateAccessToken(UserResponse user);
    String generateRefreshToken();
    long getAccessTokenExpiresIn();
}
