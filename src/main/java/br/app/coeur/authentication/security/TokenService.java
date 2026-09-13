package br.app.coeur.authentication.security;

import br.app.coeur.users.dto.UserResponse;

public interface TokenService {
    String generateAccessToken(UserResponse user);
    String generateRefreshToken();
    long getAccessTokenExpiresIn();
}
