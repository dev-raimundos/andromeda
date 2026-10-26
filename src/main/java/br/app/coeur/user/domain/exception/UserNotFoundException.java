package br.app.coeur.user.domain.exception;

import br.app.coeur.shared.exception.ResourceNotFoundException;

/** Indicates that the requested user does not exist. */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException() {
        super("User not found.");
    }
}
