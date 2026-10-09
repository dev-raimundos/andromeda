package br.app.coeur.modules.shopping.exception;

import br.app.coeur.shared.exception.ResourceNotFoundException;

/** Indica que a lista de compras pesquisada não existe ou não pertence ao usuário. */
public class ShoppingListNotFoundException extends ResourceNotFoundException {

    public ShoppingListNotFoundException() {
        super("Lista de compras não encontrada.");
    }
}
