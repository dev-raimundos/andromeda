package br.app.coeur.modules.shopping.controller;

import br.app.coeur.modules.shopping.dto.request.CreateItemRequest;
import br.app.coeur.modules.shopping.dto.request.UpdateItemRequest;
import br.app.coeur.modules.shopping.dto.response.ItemResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Endpoints for managing (CRUD) the item catalog")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new item",
            description = "Creates a new item in the catalog."
    )
    @ApiResponse(responseCode = "201", description = "Item registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    public ItemResponse create(@Valid @RequestBody CreateItemRequest request) {
        return itemService.create(request);
    }

    @GetMapping
    @Operation(
            summary = "List all items",
            description = "Returns a page with the registered catalog items."
    )
    @ApiResponse(responseCode = "200", description = "Page returned successfully")
    public Page<ItemResponse> listAll(@PageableDefault(size = 20) Pageable pageable) {
        return itemService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Find item by ID",
            description = "Returns the details of a specific catalog item."
    )
    @ApiResponse(responseCode = "200", description = "Item found successfully")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public ItemResponse getById(@PathVariable UUID id) {
        return itemService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update item data",
            description = "Modifies the data of an existing catalog item."
    )
    @ApiResponse(responseCode = "200", description = "Item updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public ItemResponse update(@PathVariable UUID id,
                               @Valid @RequestBody UpdateItemRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete an item",
            description = "Physically removes an item from the catalog."
    )
    @ApiResponse(responseCode = "204", description = "Item deleted successfully")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public void delete(@PathVariable UUID id) {
        itemService.delete(id);
    }
}
