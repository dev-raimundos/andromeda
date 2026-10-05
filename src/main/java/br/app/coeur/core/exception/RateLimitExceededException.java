package br.app.coeur.core.exception;

import br.app.coeur.shared.exception.BusinessException;

/** Indica que o cliente excedeu o limite de requisições permitido. */
public class RateLimitExceededException extends BusinessException {

    public RateLimitExceededException(String ip) {
        super("Excesso de requisições para o IP '%s'. Tente novamente em instantes.".formatted(ip));
    }
}
