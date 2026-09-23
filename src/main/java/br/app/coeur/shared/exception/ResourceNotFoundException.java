package br.app.coeur.shared.exception;

/** Indica recursos não encontrados. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
