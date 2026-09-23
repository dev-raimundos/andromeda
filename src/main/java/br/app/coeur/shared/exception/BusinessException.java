package br.app.coeur.shared.exception;

/** Indica violações genéricas de regra de negócio. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
