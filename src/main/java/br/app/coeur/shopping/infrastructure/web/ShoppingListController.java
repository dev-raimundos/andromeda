package br.app.coeur.shopping.infrastructure.web;

import br.app.coeur.shared.security.CurrentUserId;
import br.app.coeur.shopping.application.ShoppingListService;
import br.app.coeur.shopping.application.dto.request.AddItemToShoppingListRequest;
import br.app.coeur.shopping.application.dto.request.CreateShoppingListRequest;
import br.app.coeur.shopping.application.dto.request.UpdateItemQuantityRequest;
import br.app.coeur.shopping.application.dto.request.UpdateShoppingListRequest;
import br.app.coeur.shopping.application.dto.response.ShoppingListResponse;
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
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
@Tag(name = "Shopping Lists", description = "Endpoints for managing shopping lists and their items")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new shopping list",
            description = "Creates a new shopping list for the authenticated user."
    )
    @ApiResponse(responseCode = "201", description = "List created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ShoppingListResponse create(@CurrentUserId Long userId,
                                       @Valid @RequestBody CreateShoppingListRequest request) {
        return shoppingListService.create(userId, request);
    }

    @GetMapping
    @Operation(
            summary = "List the user's shopping lists",
            description = "Returns a page with the authenticated user's shopping lists."
    )
    @ApiResponse(responseCode = "200", description = "Page returned successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public Page<ShoppingListResponse> listAll(@CurrentUserId Long userId,
                                              @PageableDefault(size = 20) Pageable pageable) {
        return shoppingListService.findAll(userId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Find shopping list by ID",
            description = "Returns the details of one of the authenticated user's shopping lists, including its items."
    )
    @ApiResponse(responseCode = "200", description = "List found successfully")
    @ApiResponse(responseCode = "404", description = "List not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ShoppingListResponse getById(@CurrentUserId Long userId,
                                        @PathVariable Long id) {
        return shoppingListService.findById(userId, id);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Rename shopping list",
            description = "Modifies the name of an existing shopping list."
    )
    @ApiResponse(responseCode = "200", description = "List updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "List not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ShoppingListResponse rename(@CurrentUserId Long userId,
                                       @PathVariable Long id,
                                       @Valid @RequestBody UpdateShoppingListRequest request) {
        return shoppingListService.rename(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete shopping list",
            description = "Physically removes a shopping list and its items."
    )
    @ApiResponse(responseCode = "204", description = "List deleted successfully")
    @ApiResponse(responseCode = "404", description = "List not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public void delete(@CurrentUserId Long userId,
                       @PathVariable Long id) {
        shoppingListService.delete(userId, id);
    }

    @PostMapping("/{id}/items")
    @Operation(
            summary = "Add item to the list",
            description = "Adds a catalog item to the shopping list with the given quantity. " +
                    "If the item is already in the list, the quantity is added to the existing one."
    )
    @ApiResponse(responseCode = "200", description = "Item added successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "List or item not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ShoppingListResponse addItem(@CurrentUserId Long userId,
                                        @PathVariable Long id,
                                        @Valid @RequestBody AddItemToShoppingListRequest request) {
        return shoppingListService.addItem(userId, id, request);
    }

    @PutMapping("/{id}/items/{itemId}")
    @Operation(
            summary = "Change an item's quantity in the list",
            description = "Sets the quantity of an item already present in the shopping list."
    )
    @ApiResponse(responseCode = "200", description = "Quantity updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload")
    @ApiResponse(responseCode = "404", description = "List not found or item is not part of the list")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ShoppingListResponse changeItemQuantity(@CurrentUserId Long userId,
                                                   @PathVariable Long id,
                                                   @PathVariable Long itemId,
                                                   @Valid @RequestBody UpdateItemQuantityRequest request) {
        return shoppingListService.changeItemQuantity(userId, id, itemId, request);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(
            summary = "Remove item from the list",
            description = "Removes an item from the shopping list."
    )
    @ApiResponse(responseCode = "200", description = "Item removed successfully")
    @ApiResponse(responseCode = "404", description = "List not found or item is not part of the list")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ShoppingListResponse removeItem(@CurrentUserId Long userId,
                                           @PathVariable Long id,
                                           @PathVariable Long itemId) {
        return shoppingListService.removeItem(userId, id, itemId);
    }
}
