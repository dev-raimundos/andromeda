package br.app.coeur.core.exception;

import br.app.coeur.shared.exception.BusinessException;

/** Indicates that the client exceeded the allowed request rate. */
public class RateLimitExceededException extends BusinessException {

    public RateLimitExceededException(String ip) {
        super("Too many requests from IP '%s'. Try again shortly.".formatted(ip));
    }
}
