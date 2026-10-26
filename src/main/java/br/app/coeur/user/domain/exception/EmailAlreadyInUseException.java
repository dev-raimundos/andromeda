package br.app.coeur.user.domain.exception;

import br.app.coeur.shared.exception.ConflictException;

/** Indicates that the given email is already registered to another user. */
public class EmailAlreadyInUseException extends ConflictException {

    public EmailAlreadyInUseException() {
        super("Email is already in use.");
    }
}
