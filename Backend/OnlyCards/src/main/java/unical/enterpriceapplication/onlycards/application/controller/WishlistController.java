package unical.enterpriceapplication.onlycards.application.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.core.service.AuthService;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductSorting;
import unical.enterpriceapplication.onlycards.application.data.service.CapabilityTokenService;
import unical.enterpriceapplication.onlycards.application.data.service.ProductService;
import unical.enterpriceapplication.onlycards.application.data.service.UserService;
import unical.enterpriceapplication.onlycards.application.data.service.WishlistService;
import unical.enterpriceapplication.onlycards.application.dto.ProductDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductIdDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductWishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.ServiceError;
import unical.enterpriceapplication.onlycards.application.dto.UserDTO;
import unical.enterpriceapplication.onlycards.application.dto.UserWishlistEditDto;
import unical.enterpriceapplication.onlycards.application.dto.WishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.WishlistEditDto;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.MissingParametersException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceAlreadyExistsException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

@RestController
@RequestMapping(path = "/v1/wishlists", produces = "application/json")
@RequiredArgsConstructor
@Validated
@Slf4j
public class WishlistController {
    @Getter
    private final WishlistService wishlistService;
    private final ProductService productService;
    private final UserService userService;
    private final CapabilityTokenService capabilityTokenService;

    @Getter
    private final AuthService authService;
   @Operation(summary = "Products of a wishlist", description = "Get all products of a wishlist by the wishlist ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved"),
            @ApiResponse(responseCode = "404", description = "No wishlist found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner or a person that the wishlist is shared with", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @GetMapping(value = "/{id}/products", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(" @wishlistController.wishlistService.isUserOwnerOrShared(#id, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Page<ProductWishlistDto>> getWishlistCards(@PathVariable UUID id,
                                                                     @RequestParam() @PositiveOrZero int page,
                                                                     @RequestParam() @Positive @Max(30) int size,
                                                                     @RequestParam(required = false) @Size(max = 50) String name,
                                                                     @RequestParam(required = false) @Size(max = 50) String owner,
                                                                     @RequestParam(required = false, name = "sort" ) ProductSorting sort) throws ResourceNotFoundException {
        log.debug("Getting product with name: {} and owner: {} from wishlist with id: {}", name, owner, id);
        log.debug("Getting products of wishlist with id: {}", id);
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(id, authService.getCurrentUserUUID());
        if(wishlist.isEmpty()){
            log.debug("Wishlist with id: {} not found", id);
            throw new ResourceNotFoundException(id.toString(), "Wishlist");
        }
        Page<ProductWishlistDto> products = wishlistService.getWishlistProducts(id, page, size, sort, name, owner);
        log.debug("There are: {} from wishlist with id: {}", products.getTotalElements(), id);
        products.forEach(cardDTO -> log.trace(cardDTO.toString()));
        return ResponseEntity.ok(products);
    }
    @Operation(summary = "Delete a product in a wishlist", description = "Delete a product from a wishlist by the wishlist ID and the product ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "No wishlist found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner of the wishlist ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @DeleteMapping("/{id}/products/{productId}")
    @PreAuthorize(" @wishlistController.wishlistService.isUserOwner(#id, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Void> deleteAProduct(
            @PathVariable UUID id,
            @PathVariable UUID productId) throws ResourceNotFoundException {
        if(wishlistService.getWishlistById(id, authService.getCurrentUserUUID()).isEmpty()) {
            log.warn("Wishlist with id {} not found", id);
            throw new ResourceNotFoundException(id.toString(), "Wishlist");
        }
        log.debug("Deleting product with id: {} from wishlist with id: {}", productId, id);

        wishlistService.deleteProductFromWishlist(id, productId);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Add a product in a wishlist", description = "Add a product from a wishlist by the wishlist ID and the product ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "422", description="the limit of product in wishlist has been exceeded", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "200", description = "Product added"),
            @ApiResponse(responseCode = "404", description = "No wishlist or product found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner of the wishlist ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PostMapping(value = "/{wishlistId}/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(" @wishlistController.wishlistService.isUserOwner(#wishlistId, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Void> addCardToWishlist(@PathVariable UUID wishlistId,  @RequestBody @Valid ProductIdDto productIdDto) throws MissingParametersException, LimitExceedException, ResourceAlreadyExistsException, ResourceNotFoundException {

        Optional<ProductDto> productOptional = productService.getProductById(productIdDto.getId());
        if(productOptional.isEmpty()) {
            log.warn("Product with id {} not found", productIdDto.getId());
            throw new ResourceNotFoundException(productIdDto.getId().toString(), "Product");
        }
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(wishlistId, authService.getCurrentUserUUID());
        if(wishlist.isEmpty()){
            log.warn("Wishlist with id {} not found", wishlistId);
            throw new ResourceNotFoundException(wishlistId.toString(), "Wishlist");
        }
        if(wishlistService.checkIfProductIsInWishlist(wishlistId, productIdDto.getId())) {
            log.warn("Product with id {} already in wishlist: {}", productIdDto.getId(), wishlistId);
            throw new ResourceAlreadyExistsException(productIdDto.getId().toString(), "Product");
        }
       
        if(wishlist.get().getProducts().size()>= WishlistService.MAX_PRODUCTS_IN_WISHLIST)
            throw new LimitExceedException("Product in wishlist", WishlistService.MAX_PRODUCTS_IN_WISHLIST);
        
        log.debug("Add product {} in wishlist with id: {}", productIdDto.getId(), wishlistId);
        wishlistService.addProductToWishlist(wishlistId, productIdDto.getId());
        log.debug("Added card {} in wishlist with id: {}", productIdDto.getId(), wishlistId);
        return ResponseEntity.ok(null);

    }
    @Operation(summary = "Edit a wishlist", description = "Edit a wishlist by the wishlist ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist edited"),
            @ApiResponse(responseCode = "404", description = "No wishlist  found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner of the wishlist ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PatchMapping(value = "/{wishlistId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(" @wishlistController.wishlistService.isUserOwner(#wishlistId, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Void> updateWishlist(@PathVariable UUID wishlistId, @Valid  @RequestBody WishlistEditDto wishlistEditDto) throws ResourceAlreadyExistsException, IllegalArgumentException, ResourceNotFoundException {
        
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(wishlistId, authService.getCurrentUserUUID());
        if(wishlist.isEmpty()){
            log.warn("Wishlist with id {} not found", wishlistId);
            throw new ResourceNotFoundException(wishlistId.toString(), "Wishlist");
        }

        wishlistService.updateWishlist(authService.getCurrentUserUUID(), wishlistId, wishlistEditDto);
        log.debug("Wishlist with id: {} updated", wishlistId);
        return ResponseEntity.ok(null);
    }
    @Operation(summary = "Info of a wishlist", description = "Get the info of a wishlist by the wishlist ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Info retrieved"),
            @ApiResponse(responseCode = "404", description = "No wishlist found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner or a person that the wishlist is shared with", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @GetMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(" @wishlistController.wishlistService.isUserOwnerOrShared(#id, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<WishlistDto> getWishlistById(@PathVariable UUID id) throws ResourceNotFoundException {
        log.debug("Getting wishlist with id: {}", id);
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(id, authService.getCurrentUserUUID());
        if(wishlist.isEmpty()){
            log.debug("Wishlist with id: {} not found", id);
            throw new ResourceNotFoundException("Wishlist", id.toString());
        }
        log.debug("Wishlist with id: {} found", id);
        return ResponseEntity.ok(wishlist.get());
    }
    @Operation(summary = "Add a user in a wishlist", description = "Add a user in a wishlist by the wishlist ID and the user username ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "422", description="the limit of users in wishlist has been exceeded", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "200", description = "User added"),
            @ApiResponse(responseCode = "404", description = "No wishlist or user found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner of the wishlist ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PostMapping(value = "/{wishlistId}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(" @wishlistController.wishlistService.isUserOwner(#wishlistId, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Void> addUserToWishlist(@PathVariable UUID wishlistId,  @RequestBody @Valid UserWishlistEditDto account) throws LimitExceedException, ResourceAlreadyExistsException, ResourceNotFoundException {

        log.debug("Add user {} in wishlist with username: {}", account.getUsername(), wishlistId);
        Optional<UserDTO> user = userService.getUserByUsername(account.getUsername());
        if(user.isEmpty())
            throw new ResourceNotFoundException(account.getUsername(), "User");
        if(user.get().getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
            throw new IllegalArgumentException("Admin cannot be added to wishlist");
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(wishlistId, authService.getCurrentUserUUID());
        if(wishlist.isEmpty())
            throw new ResourceNotFoundException(wishlistId.toString(), "Wishlist");
        if(wishlist.get().getAccounts().size()>=WishlistService.MAX_SHARED_WITH)
            throw new LimitExceedException("Shared with", WishlistService.MAX_SHARED_WITH);
        if(wishlistService.isUserInWishlist(user.get().getId(), wishlistId))
            throw new ResourceAlreadyExistsException(account.getUsername(), "User in wishlist");
        wishlistService.addUserToWishlist(wishlistId, account);
        log.debug("Added user {} in wishlist with username: {}", account.getUsername(), wishlistId);
        return ResponseEntity.ok(null);

    }
    
    @Operation(summary = "Delete a user in a wishlist", description = "Delete a user in a wishlist by the wishlist ID and the user ID ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "No wishlist or user found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner of the wishlist ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @DeleteMapping(value = "/{wishlistId}/users/{userId}")
    @PreAuthorize(" @wishlistController.wishlistService.isUserOwner(#wishlistId, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Void> deleteUserToWishlist(@PathVariable UUID wishlistId,  @PathVariable UUID userId) throws ResourceNotFoundException {
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(wishlistId, authService.getCurrentUserUUID());
        if(wishlist.isEmpty())
            throw new ResourceNotFoundException(wishlistId.toString(), "Wishlist");
        log.debug("Deleting user {} in wishlist with id: {}", userId, wishlistId);
        if(wishlistService.isUserOwner(wishlistId, userId)){
            throw new IllegalArgumentException("Owner cannot be deleted from wishlist");
        }
        wishlistService.deleteUserFromWishlist(wishlistId, userId);
        log.debug("Deleting user {} in wishlist with id: {}", userId, wishlistId);
        return ResponseEntity.ok(null);

    }
    @Operation(summary = "Delete a a wishlist", description = "Delete a wishlist by the wishlist ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlist deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner of the wishlist ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(" @wishlistController.wishlistService.isUserOwner(#id, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Void> deleteWishlist(@PathVariable UUID id)  {
        log.debug("Deleting wishlist with id: {}", id);
        wishlistService.deleteWishlist(id);
        log.debug("Wishlist with id: {} deleted", id);
        return ResponseEntity.ok(null);
    }
    @Operation(summary = "Create a capability url for a wishlist", description = "Create a capability url for a wishlist by the wishlist ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Capability created"),
            @ApiResponse(responseCode = "404", description = "No Wishlist found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner of the wishlist ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PostMapping(value = "/{wishlistId}/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(" @wishlistController.wishlistService.isUserOwner(#wishlistId, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity< Map<String, String>> generateToken(@PathVariable UUID wishlistId) throws ResourceNotFoundException, ResourceAlreadyExistsException {
        log.debug("Generating token for wishlist with id: {}", wishlistId);
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(wishlistId, authService.getCurrentUserUUID());
        if(wishlist.isEmpty())
            throw new ResourceNotFoundException(wishlistId.toString(), "Wishlist");
        if(wishlist.get().getToken()!=null)
            throw new ResourceAlreadyExistsException(wishlistId.toString(), "Token");
        String token = capabilityTokenService.generateToken(wishlistId);
        log.debug("Token generated for wishlist with id: {}", wishlistId);
        return ResponseEntity.ok(Map.of("token", token));
    }
    @Operation(summary = "Delete a capability url for a wishlist", description = "Delete a capability url for a wishlist by the wishlist ID and the token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Capability deleted"),
            @ApiResponse(responseCode = "404", description = "No Wishlist found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be the owner of the wishlist ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @DeleteMapping(value = "/{wishlistId}/token/{token}")
    @PreAuthorize("@wishlistController.wishlistService.isUserOwner(#wishlistId, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Void> deleteToken(@PathVariable UUID wishlistId, @PathVariable String token) throws ResourceNotFoundException {
        log.debug("Deleting token for wishlist with id: {}", wishlistId);
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(wishlistId, authService.getCurrentUserUUID());
        if(wishlist.isEmpty())
            throw new ResourceNotFoundException(wishlistId.toString(), "Wishlist");
        capabilityTokenService.deleteToken(wishlistId, token);
        log.debug("Token deleted for wishlist with id: {}", wishlistId);
        return ResponseEntity.ok(null);
    }


    @SecurityRequirements()
    @Operation(summary = "Product of a wishlist", description = "Get all products of a wishlist by the wishlist capability token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved"),
            @ApiResponse(responseCode = "404", description = "No wishlist found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @GetMapping(value= "/token/{token}/products", produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ProductWishlistDto>> getWishlistCardsByToken(@PathVariable String token,
                                                                            @RequestParam() @PositiveOrZero int page,
                                                                            @RequestParam() @Positive @Max(30) int size,
                                                                            @RequestParam(required = false) @Size(max = 50) String name,
                                                                            @RequestParam(required = false) @Size(max = 50) String owner,
                                                                            @RequestParam(required = false, name = "sort" ) ProductSorting sort) throws ResourceNotFoundException {
        log.debug("Getting cards of wishlist with token: {}", token);
       UUID wishlistId= capabilityTokenService.getWishlistIdFromToken(token);
        if(wishlistId==null ){
           log.debug("Wishlist with token: {} not found", token);
           throw new ResourceNotFoundException(token, "Wishlist");
        }
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(wishlistId, null);
        if(wishlist.isEmpty()){
            log.debug("Wishlist with token: {} not found", token);
            throw new ResourceNotFoundException(token, "Wishlist");
        }

        Page<ProductWishlistDto> cards = wishlistService.getWishlistProducts(wishlistId, page, size, sort, name, owner);
        log.debug("There are: {} from wishlist with token: {}", cards.getTotalElements(), token);
        cards.forEach(cardDTO -> log.trace(cardDTO.toString()));
        return ResponseEntity.ok(cards);
    }
    @Operation(summary = "Info of a wishlist", description = "Get the info of a wishlist by the wishlist capability token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Info retrieved"),
            @ApiResponse(responseCode = "404", description = "No wishlist found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @SecurityRequirements()
    @GetMapping("/token/{token}")
    public ResponseEntity<WishlistDto> getWishlistByToken(HttpServletRequest request, @PathVariable String token) throws ResourceNotFoundException {
        log.debug("Getting wishlist with token: {}", token);
        UUID wishlistId= capabilityTokenService.getWishlistIdFromToken(token);
        if(wishlistId==null ){
            log.debug("Wishlist with token: {} not found", token);
            throw new ResourceNotFoundException(token, "Wishlist");
        }
        Optional<WishlistDto> wishlist = wishlistService.getWishlistById(wishlistId, null);
        if(wishlist.isEmpty()){
            log.debug("Wishlist with token: {} not found", token);
            throw new ResourceNotFoundException(token, "Wishlist");
        }
        log.debug("Wishlist with token: {} found", token);
        return ResponseEntity.ok(wishlist.get());
    }




}
