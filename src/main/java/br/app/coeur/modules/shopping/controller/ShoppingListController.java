package br.app.coeur.modules.shopping.controller;

import br.app.coeur.modules.shopping.dto.AddItemToShoppingListRequest;
import br.app.coeur.modules.shopping.dto.CreateShoppingListRequest;
import br.app.coeur.modules.shopping.dto.ShoppingListResponse;
import br.app.coeur.modules.shopping.dto.UpdateItemQuantityRequest;
import br.app.coeur.modules.shopping.dto.UpdateShoppingListRequest;
import br.app.coeur.modules.shopping.service.ShoppingListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
@Tag(name = "Listas de Compras", description = "Endpoints para gerenciamento de listas de compras e seus itens")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar uma nova lista de compras",
            description = "Cria uma nova lista de compras para o usuário autenticado."
    )
    @ApiResponse(responseCode = "201", description = "Lista criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Payload inválido")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public ShoppingListResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateShoppingListRequest request) {
        return shoppingListService.create(userId(jwt), request);
    }

    @GetMapping
    @Operation(
            summary = "Listar listas de compras do usuário",
            description = "Retorna uma página com as listas de compras do usuário autenticado."
    )
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public Page<ShoppingListResponse> listAll(@AuthenticationPrincipal Jwt jwt, @PageableDefault(size = 20) Pageable pageable) {
        return shoppingListService.findAll(userId(jwt), pageable);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar lista de compras pelo ID",
            description = "Retorna os detalhes de uma lista de compras do usuário autenticado, incluindo seus itens."
    )
    @ApiResponse(responseCode = "200", description = "Lista encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Lista não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public ShoppingListResponse getById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return shoppingListService.findById(userId(jwt), id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Renomear lista de compras",
            description = "Modifica o nome de uma lista de compras existente."
    )
    @ApiResponse(responseCode = "200", description = "Lista atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Payload inválido")
    @ApiResponse(responseCode = "404", description = "Lista não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public ShoppingListResponse rename(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody UpdateShoppingListRequest request) {
        return shoppingListService.rename(userId(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Deletar lista de compras",
            description = "Remove fisicamente uma lista de compras e seus itens."
    )
    @ApiResponse(responseCode = "204", description = "Lista deletada com sucesso")
    @ApiResponse(responseCode = "404", description = "Lista não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        shoppingListService.delete(userId(jwt), id);
    }

    @PostMapping("/{id}/items")
    @Operation(
            summary = "Adicionar item à lista",
            description = "Adiciona um item do catálogo à lista de compras com a quantidade informada. " +
                    "Se o item já estiver na lista, a quantidade é somada à existente."
    )
    @ApiResponse(responseCode = "200", description = "Item adicionado com sucesso")
    @ApiResponse(responseCode = "400", description = "Payload inválido")
    @ApiResponse(responseCode = "404", description = "Lista ou item não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public ShoppingListResponse addItem(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody AddItemToShoppingListRequest request) {
        return shoppingListService.addItem(userId(jwt), id, request);
    }

    @PutMapping("/{id}/items/{itemId}")
    @Operation(
            summary = "Alterar quantidade de um item na lista",
            description = "Define a quantidade de um item já presente na lista de compras."
    )
    @ApiResponse(responseCode = "200", description = "Quantidade atualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Payload inválido")
    @ApiResponse(responseCode = "404", description = "Lista não encontrada ou item não faz parte da lista")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public ShoppingListResponse changeItemQuantity(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @PathVariable Long itemId, @Valid @RequestBody UpdateItemQuantityRequest request) {
        return shoppingListService.changeItemQuantity(userId(jwt), id, itemId, request);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(
            summary = "Remover item da lista",
            description = "Remove um item da lista de compras."
    )
    @ApiResponse(responseCode = "200", description = "Item removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Lista não encontrada ou item não faz parte da lista")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    public ShoppingListResponse removeItem(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @PathVariable Long itemId) {
        return shoppingListService.removeItem(userId(jwt), id, itemId);
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }
}
