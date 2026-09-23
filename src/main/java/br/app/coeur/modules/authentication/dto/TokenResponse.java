package br.app.coeur.modules.authentication.dto;

public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static TokenResponse bearer(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
