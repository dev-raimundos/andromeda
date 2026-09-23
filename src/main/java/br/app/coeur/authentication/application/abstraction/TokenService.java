package br.app.coeur.authentication.application.abstraction;

import br.app.coeur.user.domain.User;

public interface TokenService {
    String generateAccessToken(User user);
    String generateRefreshToken();
    long getAccessTokenExpiresIn();
}
