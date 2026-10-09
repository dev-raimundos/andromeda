package br.app.coeur.modules.shopping.exception;

import br.app.coeur.shared.exception.ResourceNotFoundException;

/** Indica que o item pesquisado não existe. */
public class ItemNotFoundException extends ResourceNotFoundException {

    public ItemNotFoundException() {
        super("Item não encontrado.");
    }
}
