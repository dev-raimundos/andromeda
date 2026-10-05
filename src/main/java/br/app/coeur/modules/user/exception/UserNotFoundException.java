package br.app.coeur.modules.user.exception;

import br.app.coeur.shared.exception.ResourceNotFoundException;

/** Indica que o usuário pesquisado não existe. */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException() {
        super("Usuário não encontrado.");
    }
}
