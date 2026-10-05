package br.app.coeur.modules.user.exception;

import br.app.coeur.shared.exception.ConflictException;

/** Indica que o e-mail informado já está cadastrado para outro usuário. */
public class EmailAlreadyInUseException extends ConflictException {

    public EmailAlreadyInUseException() {
        super("E-mail já está em uso.");
    }
}
