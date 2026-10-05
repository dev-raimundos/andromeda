package br.app.coeur.modules.authentication.exception;

import br.app.coeur.shared.exception.BusinessException;

/** Indica que o e-mail ou a senha informados no login são inválidos. */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("Credenciais inválidas.");
    }
}
