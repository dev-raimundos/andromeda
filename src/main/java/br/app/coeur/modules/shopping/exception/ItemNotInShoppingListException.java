package br.app.coeur.modules.shopping.exception;

import br.app.coeur.shared.exception.ResourceNotFoundException;

/** Indica que o item não faz parte da lista de compras informada. */
public class ItemNotInShoppingListException extends ResourceNotFoundException {

    public ItemNotInShoppingListException() {
        super("Item não encontrado na lista de compras.");
    }
}
