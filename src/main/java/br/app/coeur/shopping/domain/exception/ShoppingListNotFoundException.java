package br.app.coeur.shopping.domain.exception;

import br.app.coeur.shared.exception.ResourceNotFoundException;

/** Indicates that the requested shopping list does not exist or does not belong to the user. */
public class ShoppingListNotFoundException extends ResourceNotFoundException {

    public ShoppingListNotFoundException() {
        super("Shopping list not found.");
    }
}
