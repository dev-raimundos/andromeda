package br.app.coeur.modules.user.exception;

import br.app.coeur.shared.exception.BusinessException;

/** Indica que a conta do usuário está temporariamente bloqueada por excesso de tentativas de login falhas. */
public class UserBlockedException extends BusinessException {

    public UserBlockedException() {
        super("Conta temporariamente bloqueada devido a múltiplas tentativas falhas. Tente novamente mais tarde.");
    }
}
