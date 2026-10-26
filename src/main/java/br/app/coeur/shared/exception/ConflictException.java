package br.app.coeur.shared.exception;

/** Indicates a business rule conflict. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
