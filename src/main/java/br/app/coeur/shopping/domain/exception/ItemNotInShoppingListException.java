package br.app.coeur.shopping.domain.exception;

import br.app.coeur.shared.exception.ResourceNotFoundException;

/** Indicates that the item is not part of the given shopping list. */
public class ItemNotInShoppingListException extends ResourceNotFoundException {

    public ItemNotInShoppingListException() {
        super("Item not found in the shopping list.");
    }
}
