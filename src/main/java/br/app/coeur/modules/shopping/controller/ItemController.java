package br.app.coeur.modules.shopping.controller;

import br.app.coeur.modules.shopping.dto.CreateItemRequest;
import br.app.coeur.modules.shopping.dto.ItemResponse;
import br.app.coeur.modules.shopping.dto.UpdateItemRequest;
import br.app.coeur.modules.shopping.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Itens", description = "Endpoints para gerenciamento (CRUD) do catálogo de itens")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar um novo item",
            description = "Cria um novo item no catálogo."
    )
    @ApiResponse(responseCode = "201", description = "Item cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Payload inválido")
    public ItemResponse create(@Valid @RequestBody CreateItemRequest request) {
        return itemService.create(request);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os itens",
            description = "Retorna uma página com os itens cadastrados no catálogo."
    )
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    public Page<ItemResponse> listAll(@PageableDefault(size = 20) Pageable pageable) {
        return itemService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar item pelo ID",
            description = "Retorna os detalhes de um item específico do catálogo."
    )
    @ApiResponse(responseCode = "200", description = "Item encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    public ItemResponse getById(@PathVariable Long id) {
        return itemService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar dados do item",
            description = "Modifica os dados de um item existente no catálogo."
    )
    @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Payload inválido")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    public ItemResponse update(@PathVariable Long id, @Valid @RequestBody UpdateItemRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Deletar um item",
            description = "Remove fisicamente um item do catálogo."
    )
    @ApiResponse(responseCode = "204", description = "Item deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }
}
