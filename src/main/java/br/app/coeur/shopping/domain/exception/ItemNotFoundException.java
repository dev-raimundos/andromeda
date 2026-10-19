package br.app.coeur.shopping.domain.exception;

import br.app.coeur.shared.exception.ResourceNotFoundException;

/** Indicates that the requested item does not exist. */
public class ItemNotFoundException extends ResourceNotFoundException {

    public ItemNotFoundException() {
        super("Item not found.");
    }
}
