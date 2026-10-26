package br.app.coeur.authentication.domain.exception;

import br.app.coeur.shared.exception.BusinessException;

/** Indicates that the email or password given at login is invalid. */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("Invalid credentials.");
    }
}
