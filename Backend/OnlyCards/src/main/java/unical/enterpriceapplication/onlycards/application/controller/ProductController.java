package unical.enterpriceapplication.onlycards.application.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.data.service.ProductPhotoService;
import unical.enterpriceapplication.onlycards.application.data.service.ProductService;
import unical.enterpriceapplication.onlycards.application.dto.ProductCartDTO;
import unical.enterpriceapplication.onlycards.application.dto.ProductDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductEditDto;
import unical.enterpriceapplication.onlycards.application.dto.SaveProductDto;
import unical.enterpriceapplication.onlycards.application.dto.ServiceError;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceInUseException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

@RestController
@RequestMapping(path = "/v1/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {
    private final ProductService productService;
    private final ProductPhotoService productPhotoService;


    @Operation(summary = "Get sale product", description = "This endpoint allows an authorized user to retrieve detailed information about a specific product, including its state description, release date, images, price, condition, and other associated details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("info/single/{productID}")
    public ProductCartDTO getSaleCard(@PathVariable UUID productID) throws ResourceNotFoundException{
        return productService.getSaleCard(productID);
    }

    @Operation(summary = "Delete product", description = "This endpoint allows an authorized user to delete a specific product from the system. The product is identified by its unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @productServiceImpl.isUserOwner(#id, @authService.getCurrentUserUUID()))")
    public void deleteProduct(@PathVariable UUID id) throws ResourceNotFoundException, ResourceInUseException {
        Optional<ProductDto> productDto = productService.getProductById(id);
        if(productDto.isEmpty()) {
            throw new ResourceNotFoundException(id.toString(), "Product");
        }
        ProductDto product = productDto.get();
        if(product.isSold())
            throw new ResourceInUseException("Un prodotto non può essere cancellato se è stato venduto");

        productService.deleteProduct(id);
    }
    @Operation(summary = "All currencies in use", description = "This endpoint retrieves all currencies of products that are available in the system. The response will include a map of language names and values.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "currencies retrieved"),
              @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be a admin", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @GetMapping(value = "/currencies", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getCurrencies() {
        return ResponseEntity.ok(productService.getCurrencies());
    }
    @Operation(summary = "Edit a product", description = "Edit a product by the product ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "422", description="the limit of number of image of the product has been exceeded", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
        @ApiResponse(responseCode = "409", description = "It' not possible to delete a sold product", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "200", description = "Product edited"),
            @ApiResponse(responseCode = "404", description = "No Product  found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be a admin or the owner of the product ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PatchMapping(value = "/{productId}",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER') or @productController.productService.isUsersOwnProduct(#productId, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Void> updateProduct(@PathVariable UUID productId, @RequestBody @Valid @ModelAttribute ProductEditDto productEditDto) throws LimitExceedException, ResourceNotFoundException, ResourceInUseException {
        //log all files
        log.info("ProductEditDto: {}", productEditDto);
        if(productService.isSold(productId))
            throw new ResourceInUseException("Un prodotto non può essere modificato se è stato venduto");
        if(productPhotoService.getImagesNumberById(productId) + productEditDto.getImages().size()> productService.getMaxImageForProduct()) {
            throw new LimitExceedException("photo for product", productService.getMaxImageForProduct());
        }
        productService.updateProduct(productId, productEditDto);


        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Delete a product image", description = "Delete a product image by the product ID and the image ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "409", description = "It' not possible to delete a image of a sold product", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "204", description = "Image deleted"),
            @ApiResponse(responseCode = "404", description = "No Image or product found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be a admin or the owner of the product ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @DeleteMapping(value = "/{productId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER') or @productController.productService.isUsersOwnProduct(#productId, @wishlistController.authService.getCurrentUserUUID())")
    public ResponseEntity<Void> deleteProductImage(@PathVariable UUID productId, @PathVariable UUID imageId) throws ResourceNotFoundException, ResourceInUseException {
        log.info("Deleting image with id: {} from product with id: {}", imageId, productId);
        if(productService.isSold(productId))
            throw new ResourceInUseException("Un prodotto non può essere modificato se è stato venduto");
        if(!productPhotoService.isProductImageExists(productId, imageId)) {
            throw new ResourceNotFoundException(imageId.toString(), "Image");
        }
        productPhotoService.deleteProductImage(productId, imageId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Save a product", description = "This endpoint allows an authorized seller or admin to submit a new product to the system. The product details, including description, condition, price, product type, and associated images, are provided via a form. The product is then validated and saved to the system under the user's account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product saved"),
            @ApiResponse(responseCode = "400", description = "Product not saved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/product/{userId}", produces = "application/json", consumes = "multipart/form-data")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('SELLER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> saveProduct(@PathVariable UUID userId, @ModelAttribute @Validated SaveProductDto saveProductDto) {
        log.info("Saving product with userId: {}, description: {}, condition: {}, price: {}, product: {}, images: {}", userId, saveProductDto.getDescription(), saveProductDto.getCondition(), saveProductDto.getPrice(), saveProductDto.getProductType(), saveProductDto.getImages());

        productService.saveProduct(String.valueOf(userId), saveProductDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // metodo per ottenere tutte le informazioni di una carta (restituisce una lista di carte)
    @Operation(summary = "Get card information", description = "This endpoint allows an authorized user to retrieve detailed information about a specific card, including its state description, release date, images, price, condition, and other associated details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card info retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping(value = "/info/{id}", produces = "application/json")
    public ResponseEntity<Page<ProductDto>> getCardInfo(
            @PathVariable @NotNull UUID id,
            @RequestParam(name = "page") @NotNull @PositiveOrZero int page) throws ResourceNotFoundException {
        log.info("Getting card info: id: {}, page: {}", id, page);

        Page<ProductDto> productDto = productService.getCardInfo(id, page);

        if(productDto.isEmpty()) {
            log.warn("Card not found: id: {}", id);
            throw new ResourceNotFoundException(id.toString(), "Card");
        }
        
        return ResponseEntity.ok(productDto);
    }

    // metodo per ottenere le ultime carte aggiunte
    @Operation(summary = "Get last added cards", description = "This endpoint allows an authorized user to retrieve the most recently added cards to the system. The cards are sorted by the date they were added, with the most recent cards appearing first.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Last added cards retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping(value = "/info/lastAdd/{game}", produces = "application/json")
    public ResponseEntity<List<ProductDto>> getLastAdd(@PathVariable String game) throws ResourceNotFoundException {
        log.info("Getting last add for game: {}", game);

        List<ProductDto> productDtos = productService.getLastAddedProducts(game);

        if (productDtos.isEmpty()) {
            log.warn("No cards found for game: {}", game);
            throw new ResourceNotFoundException(game, "Game");
        }

        return ResponseEntity.ok(productDtos);
    }

    // metodo per ottenere i prodotti di un utente
    @Operation(summary = "Get user products", description = "This endpoint allows an authorized user to retrieve all products associated with a specific user. The products are sorted by the date they were added, with the most recent products appearing first.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User products retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "productUser/{userId}", produces = "application/json")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('SELLER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<List<ProductDto>> getProductUser(@PathVariable UUID userId, @RequestParam(name = "page") @NotNull @PositiveOrZero int page) throws ResourceNotFoundException {
        log.info("Getting products for user: {}", userId);

        List<ProductDto> productDtos = productService.getProductsByUser(userId, page);

        if (productDtos.isEmpty()) {
            log.warn("No products found for user: {}", userId);
            throw new ResourceNotFoundException(userId.toString(), "User");
        }

        return ResponseEntity.ok(productDtos);
    }

    // metodo per contare il numero di prodotti di un utente
    @Operation(summary = "Count user products", description = "This endpoint allows an authorized user to count the number of products associated with a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User products counted"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value="productUser/total/{userId}")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('SELLER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<Integer> countProductUser(@PathVariable UUID userId) {
        log.info("Counting products for user: {}", userId);

        return ResponseEntity.ok(productService.countProductById(userId));
    }

    // metodo per ottenre un singolo prodotto
    @Operation(summary = "Get single product", description = "This endpoint allows an authorized user to retrieve detailed information about a specific product, including its state description, release date, images, price, condition, and other associated details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Single product retrieved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/single/{productId}")
    @PreAuthorize("(hasRole('SELLER') or hasRole('ADMIN')) and #userId == @authService.getCurrentUserUUID() and @productServiceImpl.isUserOwner(#productId, #userId)")
    public ResponseEntity<ProductDto> getSingleProduct(@PathVariable UUID productId, @RequestParam @NotNull UUID userId) throws ResourceNotFoundException {
        log.info("Getting single product with id: {}", productId);

        Optional<ProductDto> productDto = productService.getProductById(productId);

        if(productDto.isEmpty()) {
            log.warn("Product not found: id: {}", productId);
            throw new ResourceNotFoundException(productId.toString(), "Product");
        }

        return ResponseEntity.ok(productDto.get());
    }
}
