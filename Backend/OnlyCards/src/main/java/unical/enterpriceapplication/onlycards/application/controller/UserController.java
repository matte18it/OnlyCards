package unical.enterpriceapplication.onlycards.application.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.core.service.AuthService;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductSorting;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.WishListSorting;
import unical.enterpriceapplication.onlycards.application.data.service.AddressService;
import unical.enterpriceapplication.onlycards.application.data.service.ProductService;
import unical.enterpriceapplication.onlycards.application.data.service.UserService;
import unical.enterpriceapplication.onlycards.application.data.service.WishlistService;
import unical.enterpriceapplication.onlycards.application.dto.AddressDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductWishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.RoleDto;
import unical.enterpriceapplication.onlycards.application.dto.ServiceError;
import unical.enterpriceapplication.onlycards.application.dto.UserDTO;
import unical.enterpriceapplication.onlycards.application.dto.UserPublicProfileDto;
import unical.enterpriceapplication.onlycards.application.dto.UserRegistrationDto;
import unical.enterpriceapplication.onlycards.application.dto.WishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.WishlistEditDto;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceAlreadyExistsException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;


@RestController
@RequestMapping(path = "/v1/users", produces = "application/json")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final WishlistService wishlistService;
    @Getter
    private final AuthService authService;
    private final UserService userService;
    private final AddressService addressService;
    private final ProductService productService;


    @Operation(summary = "Retrieve all users",
            description = "Fetches a paginated list of users. The result can be filtered by page and size parameters. This operation requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No users found", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid pagination or filter parameters", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only admin can perform this action", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "404", description = "No users found with the provided filters", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))})
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") @Max(30) int size,
            @RequestParam(defaultValue = "username", name = "orderBy") String orderBy,
            @RequestParam(defaultValue = "asc", name = "direction") Sort.Direction sortDirection,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        log.debug("Ricevuta richiesta di caricamento utenti. Parametri: page = {}, size = {}, orderBy = {}, direction = {}, username = {}, email = {}",
                page, size, orderBy, sortDirection, username, email);

        Sort sortOrder = Sort.by(sortDirection, orderBy);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<UserDTO> userDtoPage;
        if ((username != null && !username.isEmpty()) || (email != null && !email.isEmpty())) {
            log.debug("Ricerca utenti con filtro username: {} o email: {}", username, email);
            userDtoPage = userService.searchUsersByUsernameOrEmail(username, email, pageable);
        } else {
            log.debug("Caricamento di tutti gli utenti senza filtri.");
            userDtoPage = userService.getAllUsers(pageable);
        }

        log.debug("Numero utenti trovati: {}", userDtoPage.getTotalElements());
        return ResponseEntity.ok(userDtoPage);
    }


    @Operation(summary = "Update a user",
            description = "Updates an existing user identified by their unique user ID with new data provided in the request body. This operation requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid user data provided", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only admin can update user details", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "404", description = "User not found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))})
    })
    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID userId,
            @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }


    @Operation(summary = "Register a user", description = "Method to register a new user in the system. The request body must contain the user's email, username, password, and cellphone number. The system will generate a new user with the provided information and return a 200 status code if the registration is successful.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PostMapping
    @SecurityRequirements()
    public ResponseEntity<Void> createAccount(@Validated @RequestBody UserRegistrationDto user) throws ResourceAlreadyExistsException {
        System.out.println("Received UserRegistrationDto: " + user);

        Optional<UserDTO> user1 = userService.findByEmail(user.getEmail());
        if(user1.isPresent())
            throw new ResourceAlreadyExistsException(user.getEmail(), "User");
        user1 = userService.findByUsername(user.getUsername());
        if(user1.isPresent())
            throw new ResourceAlreadyExistsException(user.getUsername(), "User");
        userService.saveUser(user, false);

        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Delete a user account",
            description = "Deletes a user account identified by their unique user ID. This operation is irreversible and requires admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only admin can delete user accounts", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "404", description = "User not found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))})
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        Optional<UserDTO> existingUserDtoOptional = userService.findById(id);

        if (existingUserDtoOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userService.deleteUserById(id);

        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Wishlists of a user", description = "A user can have multiple wishlists. This endpoint allows an authorized user to retrieve all the wishlists of a specific user, identified by their unique ID. The response includes the wishlists' details, such as the name, description, and creation date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlists found"),
            @ApiResponse(responseCode = "204", description = "No wishlists found", content={ @Content(mediaType = "application/json",schema= @Schema())}),
            @ApiResponse(responseCode = "404", description = "User not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @GetMapping("/{userId}/wishlists")
    @PreAuthorize("  (@userController.authService.getUserCorrespondsToPrincipal(#userId)) ")
    public ResponseEntity<Page<WishlistDto>> getUserWishlists(
        @RequestParam() @PositiveOrZero int page,
        @RequestParam() @Positive @Max(30) int size,
            @RequestParam(required = false, name = "sort" ) WishListSorting sorting,
            @RequestParam(required = false, name = "is-owner") Boolean isOwner,
            @PathVariable UUID userId){

        log.debug("Getting wishlist for user: {}", userId);
        Page<WishlistDto> wishlistPage;
        if(isOwner==null)
            wishlistPage = wishlistService.getUserWishlists(userId, sorting, page, size);
        else
            wishlistPage = wishlistService.getUserWishlists(userId, sorting, isOwner, page, size);

        wishlistPage.forEach(wishlistDto -> log.trace(wishlistDto.toString()));
        if(wishlistPage.isEmpty()){
            log.debug("No wishlists found for user: {}", userId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(wishlistPage);
    }
       @Operation(summary = "Create a wishlist", description = "Create a new wishlist")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "422", description="the limit of wishlist for a user has been exceeded", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "200", description = "Wishlist created"),
            @ApiResponse(responseCode = "404", description = "No User found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PreAuthorize("@userController.authService.getUserCorrespondsToPrincipal(#userId)")
    @PostMapping(value="/{userId}/wishlists", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createWishlist(@PathVariable UUID userId, @RequestBody @Valid WishlistEditDto wishlist) throws LimitExceedException, ResourceAlreadyExistsException, ResourceNotFoundException {
        log.debug("Creating wishlist");
        Optional<UserDTO> userOptional = userService.getUserById(userId);
        if(userOptional.isEmpty())
            throw new ResourceNotFoundException(userId.toString(), "User");
        if(userOptional.get().getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
            throw new IllegalArgumentException("Admin cannot be added to wishlist");


        int count = wishlistService.countWishlistsByOwner(userOptional.get().getId());
        log.debug("User has {} wishlists", count);
        if(count>=WishlistService.MAX_WISHLIST_FOR_USER)
            throw new LimitExceedException("Wishlist", WishlistService.MAX_WISHLIST_FOR_USER);
        if(wishlistService.existsByUserOwnerAndName(userOptional.get().getId(), wishlist.getName()))
            throw new ResourceAlreadyExistsException(wishlist.getName(), "Wishlist created by user "+ userOptional.get().getUsername());

        wishlistService.saveDto(wishlist, userOptional.get().getId());
        log.debug("Wishlist created");
        return ResponseEntity.ok(null);
    }



    @Operation(summary = "Public wishlist of a user", description = "A user can have multiple public wishlists. This endpoint allows an authorized user to retrieve all the public wishlists of a specific user, identified by their unique ID. The response includes the wishlists' details, such as the name, description, and creation date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlists found"),
            @ApiResponse(responseCode = "204", description = "No wishlists found",  content={ @Content(mediaType = "application/json",schema= @Schema())}),
            @ApiResponse(responseCode = "404", description = "User not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @SecurityRequirements()
    @GetMapping("/{username}/public-wishlists")
    public ResponseEntity<Page<WishlistDto>> getUserPublicWishlists(
        @RequestParam() @PositiveOrZero int page,
        @RequestParam() @Positive @Max(30) int size,
            @RequestParam(required = false, name = "sort" ) WishListSorting sorting,
            @PathVariable String username) throws ResourceNotFoundException {
        Optional<UserDTO> user = userService.findByUsername(username);
        if(user.isEmpty())
            throw new ResourceNotFoundException(username, "User");

        log.debug("Getting wishlist for user: {}", user.get().getId());
        Page<WishlistDto> wishlistPage;
    
             wishlistPage = wishlistService.getUserPublicWishlists( user.get().getId(), sorting, page, size);

        wishlistPage.forEach(wishlistDto -> log.trace(wishlistDto.toString()));
        if(wishlistPage.isEmpty()){
            log.debug("No wishlists found for user: {}",  user.get().getId());
            return ResponseEntity.noContent().build();
            }
        return ResponseEntity.ok(wishlistPage);
    }


    @Operation(summary = "A public wishlist of a user", description = "A user can have multiple public wishlists. This endpoint allows an authorized user to retrieve a specific public wishlist of a specific user, identified by their unique ID and the wishlist name. The response includes the wishlist's details, such as the name, description, and creation date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wishlists found"),
            @ApiResponse(responseCode = "404", description = "User or wishlist not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @SecurityRequirements()
    @GetMapping("/{username}/public-wishlists/{wishlistName}")
    public ResponseEntity<WishlistDto> getWishlistById(@PathVariable String username, @PathVariable String wishlistName) throws ResourceNotFoundException {
        Optional<UserDTO> user = userService.findByUsername(username);
        if(user.isEmpty())
            throw new ResourceNotFoundException(username, "User");
    
        Optional<WishlistDto> wishlist = wishlistService.getPublicWishlistByName(user.get().getId(), wishlistName);
        if(wishlist.isEmpty()){
            log.debug("Wishlist with name: {} not found", wishlistName);
            throw new ResourceNotFoundException("Wishlist", wishlistName);
        }
    
        log.debug("Wishlist with name: {} found", wishlistName);
        return ResponseEntity.ok(wishlist.get());
    }


    @Operation(summary = "Products of a public wishlist", description = "A public wishlist can contain multiple products. This endpoint allows an authorized user to retrieve all the products of a specific public wishlist, identified by the user's username and the wishlist name. The response produces a list of products, including the product's name, description, price, and creation date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "product retrieved"),
            @ApiResponse(responseCode = "404", description = "User or wishlist not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @SecurityRequirements()
    @GetMapping("/{username}/public-wishlists/{wishlistName}/products")
    public ResponseEntity<Page<ProductWishlistDto>> getWishlistCards(@PathVariable String username,
                                                                    @PathVariable String wishlistName,
                                                                     @RequestParam() @PositiveOrZero int page,
                                                                     @RequestParam() @Positive @Max(30) int size,
                                                                     @RequestParam(required = false) @Size(max = 50) String name,
                                                                     @RequestParam(required = false) @Size(max = 50) String owner,
                                                                     @RequestParam(required = false, name = "sort" ) ProductSorting sort) throws ResourceNotFoundException {
        log.debug("Getting product with name: {} and owner: {} from wishlist with name: {}", name, owner, wishlistName);
        log.debug("Getting products of wishlist with name: {}", wishlistName);
        Optional<UserDTO> user = userService.findByUsername(username);
        if(user.isEmpty())
            throw new ResourceNotFoundException(username, "User");
    
        Optional<WishlistDto> wishlist = wishlistService.getPublicWishlistByName(user.get().getId(), wishlistName);
        if(wishlist.isEmpty()){
            log.debug("Wishlist with name: {} not found", wishlistName);
            throw new ResourceNotFoundException("Wishlist", wishlistName);
        }

        Page<ProductWishlistDto> products = wishlistService.getWishlistProducts(wishlist.get().getId(), page, size, sort, name, owner);
        log.debug("There are: {} from wishlist with id: {}", products.getTotalElements(), wishlist.get().getId());
        products.forEach(cardDTO -> log.trace(cardDTO.toString()));
        return ResponseEntity.ok(products);
    }


    @Operation(summary = "Get user by id", description = "This endpoint allows an authorized user to retrieve detailed information about a specific user, identified by their unique ID. The response includes user details such as email, username, cellphone number, roles, addresses, and account status (blocked or not).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("single/{id}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('BUYER') or hasRole('ADMIN')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id, @RequestParam @NotNull UUID userId){
        log.info("Getting user by id: {}", id);

        Optional<UserDTO> user = userService.findById(id);
        if(user.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        return ResponseEntity.ok(user.get());
    }


    @Operation(summary = "Update user by id", description = "This endpoint allows an authorized user to update the details of a specific user, identified by their unique ID. The request body must contain the updated user details, including email, username, cellphone number, roles, addresses, and account status (blocked or not).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(value = "/{id}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('BUYER') or hasRole('ADMIN')) and #id == @authService.getCurrentUserUUID()")
    public ResponseEntity<UserDTO> updateUserById(@PathVariable UUID id, @RequestBody @Validated UserDTO userDTO, @RequestParam @NotNull UUID userId){
        log.info("Updating user by id: {}", id);

        // Controllo se è un oauth user e se l'email è diversa, se è diversa lancio eccezione
        if(userDTO.isOauthUser() && !userDTO.getEmail().equals(userService.getEmail(id)))
            throw new AccessDeniedException("Cannot change email of oauth user");

        Optional<UserDTO> user = userService.findById(id);
        if(user.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }


    @Operation(summary = "Retrieve addresses of a user",
            description = "Fetches all the addresses associated with a specific user, identified by their unique ID. This operation requires admin privileges or the user must be the owner of the account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Addresses retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No addresses found for the user", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only the owner or admin can access the addresses", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "404", description = "User not found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceError.class))})
    })
    @GetMapping("/{userId}/addresses")
    @PreAuthorize("hasRole('ADMIN') or (@userController.authService.getUserCorrespondsToPrincipal(#userId))")
    public ResponseEntity<List<AddressDto>> getUserAddresses(@PathVariable UUID userId) {

        // Recupera gli indirizzi dal servizio
        List<AddressDto> addresses = addressService.getAddressesForUser(userId);

        // Verifica se gli indirizzi sono presenti
        if (addresses == null || addresses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(addresses);
    }


    @Operation(summary = "Roles of a user", description = "This endpoint allows an authorized user (or a admin) to retrieve all the roles of a specific user, identified by their unique ID. The response includes the user's roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "404", description = "User not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),

    })
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN') or (@userController.authService.getUserCorrespondsToPrincipal(#userId))")
    public ResponseEntity<List<RoleDto>> getUserRoles(@PathVariable UUID userId) {

        // Recupera i ruoli dal servizio
        List<RoleDto> roles = userService.getUserRoles(userId);

        // Verifica se i ruoli sono presenti
        if (roles == null || roles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(roles);
    }


    @Operation(summary = "Update address by id",  description = "This endpoint allows an authorized user to update the details of a specific address, identified by its unique ID. The request body must contain the updated address information, including the street, city, postal code, state, country, and two flags: one indicating whether the address is the default address and another indicating whether the address is eligible for weekend delivery.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Address not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(value = "address/{addressId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('BUYER') or hasRole('ADMIN')) and #userId == @authService.getCurrentUserUUID() and @addressServiceImpl.isUserOwner(#addressId, #userId)")
    public ResponseEntity<Void> updateAddressById(@RequestBody @Validated AddressDto address, @PathVariable UUID addressId, @RequestParam @NotNull UUID userId){
        log.info("Updating address by id: {}", address.getId());

        addressService.updateAddressById(address, addressId);
        return ResponseEntity.ok(null);
    }


    @Operation(summary = "Add address", description = "This endpoint allows an authorized user to add a new address to their account. The request body must contain the address information, including the street, city, postal code, state, country, and two flags: one indicating whether the address is the default address and another indicating whether the address is eligible for weekend delivery.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address added"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "address/{userId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('BUYER') or hasRole('ADMIN')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> addAddress(@RequestBody @Validated AddressDto address, @PathVariable UUID userId){
        log.info("Adding address");

        addressService.addAddress(address, userId);
        return ResponseEntity.ok(null);
    }


    @Operation(summary = "Delete address by id", description = "This endpoint allows an authorized user to delete a specific address, identified by its unique ID. The address must be removed from the user's account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address deleted"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Address not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping(value = "address/{addressId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('BUYER') or hasRole('ADMIN')) and #userId == @authService.getCurrentUserUUID() and @addressServiceImpl.isUserOwner(#addressId, #userId)")
    public ResponseEntity<Void> deleteAddressById(@PathVariable UUID addressId, @RequestParam @NotNull UUID userId){
        log.info("Deleting address by id: {}", addressId);

        if(addressService.getAddressById(addressId).getDefaultAddress())
            throw new AccessDeniedException("Cannot delete default address");

        addressService.deleteAddressById(addressId);
        return ResponseEntity.ok(null);
    }


    @Operation(summary = "User public profile info", description = "This endpoint allows to retrieve the public profile information of a specific user, identified by their username. The response includes the user's public profile details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @GetMapping("/{username}")
    @SecurityRequirements()
    public ResponseEntity<UserPublicProfileDto> getPublicInfoUser(@PathVariable String username) throws ResourceNotFoundException {
        log.debug("Getting public info for user: {}", username);
        Optional<UserPublicProfileDto> user = userService.findByUsernamePublicProfile(username);
        if(user.isEmpty())
            throw new ResourceNotFoundException(username, "User");
        if(userService.isOnlyAdmin(user.get().getUsername()))
            throw new ResourceNotFoundException(username, "User"); // Admin don't have public profile
        return ResponseEntity.ok(user.get());
    }


    @Operation(summary = "Product that a user sells", description = "This endpoint allows to retrieve the list of products that the user sells, the user is identified by his username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "products retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @SecurityRequirements()
    @GetMapping("/{username}/products")
    public ResponseEntity<Page<ProductInfoDto>> getUserProducts(@PathVariable String username,
    @RequestParam() @PositiveOrZero int page,
    @RequestParam() @Positive @Max(30) int size) throws ResourceNotFoundException {
        log.debug("Getting products for user: {}", username);
        Optional<UserDTO> user = userService.findByUsername(username);
        if(user.isEmpty())
            throw new ResourceNotFoundException(username, "User");
        return ResponseEntity.ok(productService.getUserProducts(user.get().getId(), page, size));
    }
}