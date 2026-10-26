package br.app.coeur.authentication.domain.exception;

import br.app.coeur.shared.exception.BusinessException;

/** Indicates that the given refresh token is invalid, already revoked, or expired. */
public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid, revoked, or expired.");
    }
}
