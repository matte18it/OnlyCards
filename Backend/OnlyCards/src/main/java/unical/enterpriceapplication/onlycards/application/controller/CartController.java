package unical.enterpriceapplication.onlycards.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Cart;
import unical.enterpriceapplication.onlycards.application.data.service.CartService;
import unical.enterpriceapplication.onlycards.application.dto.ProductCartDTO;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/carts", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    private final CartService cartService;

    @Operation(summary = "Create a new cart", description = "Creates a new shopping cart for the specified user, only accessible by users with the BUYER or ADMIN role, provided that the userID matches the UUID of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart created"),
            @ApiResponse(responseCode = "400", description = "Cart not created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @PostMapping("users/{userID}")
    @PreAuthorize("(hasRole('BUYER') or hasRole('ADMIN')) and #userID == @authService.getCurrentUserUUID()")
    public ResponseEntity<Cart> createCart(@PathVariable UUID userID) throws ResourceNotFoundException {
        return ResponseEntity.ok(cartService.createCart(userID));
    }

    @Operation(summary = "Get cart products", description = "Returns a list of products in the cart of the specified user, only accessible by users with the BUYER or ADMIN role, provided that the userID matches the UUID of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart products retrieved"),
            @ApiResponse(responseCode = "400", description = "Cart products not retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @GetMapping(path = "users/{userID}/products")
    @PreAuthorize("(hasRole('BUYER') or hasRole('ADMIN')) and #userID == @authService.getCurrentUserUUID()")
    public ResponseEntity<List<ProductCartDTO>> getCartProducts(@PathVariable UUID userID) throws ResourceNotFoundException {
        return ResponseEntity.ok(cartService.getCartCards(userID));
    }

    @Operation(summary = "Add product to cart", description = "Adds a product to the cart of the specified user, only accessible by users with the BUYER role, provided that the userID matches the UUID of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product added to cart"),
            @ApiResponse(responseCode = "400", description = "Product not added to cart"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @PostMapping(path = "users/{userID}/add-products/{productID}")
    @PreAuthorize("hasRole('BUYER') and #userID == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> addProduct(@PathVariable UUID userID, @PathVariable UUID productID) throws ResourceNotFoundException, LimitExceedException {
        log.debug("Product added: {}", productID);
        cartService.addProduct(userID,productID);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove product from cart", description = "Removes a product from the cart of the specified user, only accessible by users with the BUYER role, provided that the userID matches the UUID of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product removed from cart"),
            @ApiResponse(responseCode = "400", description = "Product not removed from cart"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @PostMapping(path = "users/{userID}/remove-products/{productID}")
    @PreAuthorize("hasRole('BUYER') and #userID == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> removeProduct(@PathVariable UUID userID, @PathVariable UUID productID) throws ResourceNotFoundException {
        log.debug("Product removed: {}", productID);
        cartService.removeProduct(userID,productID);
        return ResponseEntity.ok().build();
    }
}
