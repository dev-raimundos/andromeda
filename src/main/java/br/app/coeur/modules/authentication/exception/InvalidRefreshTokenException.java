package br.app.coeur.modules.authentication.exception;

import br.app.coeur.shared.exception.BusinessException;

/** Indica que o refresh token informado é inválido, já foi revogado ou está expirado. */
public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException() {
        super("Refresh token inválido, revogado ou expirado.");
    }
}
