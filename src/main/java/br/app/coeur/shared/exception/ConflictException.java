package br.app.coeur.shared.exception;

/** Indica conflitos de regra de negócio. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
