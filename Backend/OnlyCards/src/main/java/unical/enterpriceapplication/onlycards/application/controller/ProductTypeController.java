package unical.enterpriceapplication.onlycards.application.controller;

import java.util.*;

import javax.naming.LimitExceededException;

import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.core.service.SlugifyService;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductSorting;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductTypeSpecification;
import unical.enterpriceapplication.onlycards.application.data.service.FeatureProductService;
import unical.enterpriceapplication.onlycards.application.data.service.ProductTypeService;
import unical.enterpriceapplication.onlycards.application.dto.*;
import unical.enterpriceapplication.onlycards.application.exception.BadRequestException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceInUseException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;
import unical.enterpriceapplication.onlycards.application.exception.UnsupportedMediaTypeException;

@RestController
@RequestMapping(path = "/v1/product-types")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductTypeController {
    private final ProductTypeService productTypeService;
    private final FeatureProductService featureProductService;
    private final SlugifyService slugifyService;
     @Operation(summary = "All games", description = "This endpoint retrieves all games (of the products) that are available in the system. The response will include a map of game names and their respective slugs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "games retrieved"),
    })
    @SecurityRequirements()
    @GetMapping(value = "/games", produces = "application/json")
    public Map<String, String> getGames() {
        List<String> games = productTypeService.findDistinctGame();
        Map<String, String> gamesMap = new HashMap<>();
        for (String game : games) {
            gamesMap.put(slugifyService.slugify(game), game);
        }
        return gamesMap;
    }
    @Operation(summary = "All types", description = "This endpoint retrieves all types of products that are available in the system. The response will include a map of type names and their respective slugs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "types retrieved"),
    })
    @SecurityRequirements()
    @GetMapping(value = "/types", produces = "application/json")
    public Map<String, String> getTypes() {
        List<String> types = productTypeService.findDistinctType();
        Map<String, String> typesMap = new HashMap<>();
        for (String type : types) {
            typesMap.put(slugifyService.slugify(type), type);
        }
        return typesMap;
    }
    @Operation(summary = "All languages", description = "This endpoint retrieves all languages of products that are available in the system. The response will include a map of language names and their respective slugs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "languages retrieved"),
    })
    @SecurityRequirements()
    @GetMapping(value = "/languages", produces = "application/json")
    public Map<String, String> getLanguages() {
        List<String> languages = productTypeService.findDistinctLanguage();
        Map<String, String> languagesMap = new HashMap<>();
        for (String language : languages) {
            languagesMap.put(slugifyService.slugify(language), language);
        }
        return languagesMap;
    }
    @Operation(summary = "All sorting options", description = "This endpoint retrieves all sorting options supported of the products that are available in the system. The response will include a map of sorting options names and their respective normalize name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "sorting options retrieved"),
    })
    @SecurityRequirements()
    @GetMapping(value = "/sorting-options", produces = "application/json")
    public Map<String, String> getSortingOptions() {
        return ProductSorting.getSortingOptions();
    }
    @Operation(summary = "All the features of a given game", description = "This endpoint retrieves all the features of the products that are available in the system. The response will include a list of features with all their possibile values.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "sorting options retrieved"),
    })
    @SecurityRequirements()
    @GetMapping(value = "/{game}/features", produces = "application/json")
    public ResponseEntity<List<FeatureSearchDTO>> getFeatures(@PathVariable String game) {
        List<String> games = productTypeService.findDistinctGame();
        for(String g : games){
            if(slugifyService.slugify(g).equals(game)){
                game = g;
                break;
            }
        }
        return ResponseEntity.ok(productTypeService.getFeatures(game));
    }
    @Operation(summary = "products types", description = "This endpoint retrieves the products types that are available in the system. The response will include a list of product types with the possibility of filter and sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "sorting options retrieved"),
    })
    @SecurityRequirements()
    @GetMapping(value = "/{game}/products", produces = "application/json")
    public ResponseEntity<Page<ProductTypeDto>> getProductTypes(
    @PathVariable String game,
    @RequestParam(required = false) String lan,
    @RequestParam(required = false) String type,
    @RequestParam(required = false) @Size(max = 50) String name,
    @RequestParam(required = false, name = "min-price") @PositiveOrZero Long minPrice,
    @RequestParam(required = false,  name = "max-price") @PositiveOrZero Long maxPrice,
    @RequestParam(required = false) Map<String, String> features,
    @RequestParam() @PositiveOrZero int page,
    @RequestParam() @Positive @Max(30) int size,
    @RequestParam(required = false, name = "sort" ) ProductSorting ordinamento) {
        // Rimozione delle chiavi gestite separatamente dai parametri dalla mappa delle features
        features.remove("lan");
        features.remove("name");
        features.remove("min-price");
        features.remove("max-price");
        features.remove("sort");
        features.remove("page");
        features.remove("size");
        features.remove("type");
        List<String> games = productTypeService.findDistinctGame();
        for(String g : games){
            if(slugifyService.slugify(g).equals(game)){
                game = g;
                break;
            }
        }
        List<String> languages = productTypeService.findDistinctLanguage();
        for(String l : languages){
            if(slugifyService.slugify(l).equals(lan)){
                lan = l;
                break;
            }
        }
        List<String> types = productTypeService.findDistinctType();
        for(String t : types){
            if(slugifyService.slugify(t).equals(type)){
                type = t;
                break;
            }
        }

        ProductTypeSpecification.Filter filter = new ProductTypeSpecification.Filter();
        filter.setGame(game);
        filter.setType(type);
        filter.setLanguage(lan);
        filter.setName(name);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setFeatures(features);
        filter.setProductSorting(ordinamento);
        Page<ProductTypeDto> productTypeDtos = productTypeService.getProductTypes(filter, page, size);
        return ResponseEntity.ok(productTypeDtos);
    }
      @Operation(summary = "create a product type", description = "A product type is a template for multiple products. This endpoint allows an authorized user to create a new product type. The response will include details such as the product type's name, type, price, minimum price, number of sales, associated game, and any relevant features.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "422", description="the limit of features for a product has been exceeded", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "200", description = "Product type created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "400", description = "The data provided are invalid, or, the extension of the photo is not supported, or the photo size exceeds the allowed limits.", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be a admin or a seller", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PostMapping(  consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductTypeDto> createProductType(@RequestBody @Valid @ModelAttribute ProductTypeRegistrationDto productTypeRegistrationDto)throws  UnsupportedMediaTypeException, LimitExceededException, BadRequestException {
        if(productTypeRegistrationDto.getFeatures().size()>featureProductService.getMaxFeaturesForProduct()) {
            throw new LimitExceededException("A product can have at most " + featureProductService.getMaxFeaturesForProduct() + " features");
        }
        if(productTypeRegistrationDto.getPhoto() == null && productTypeRegistrationDto.getPhotoUrl() == null) {
            throw new BadRequestException("The photo is required");
        }
        return ResponseEntity.ok(productTypeService.saveProductType(productTypeRegistrationDto));
    }
    @Operation(summary = "modify a existing product type", description = "A product type is a template for multiple products. This endpoint allows an admin to modify a  product type.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "422", description="the limit of features for a product has been exceeded", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
        @ApiResponse(responseCode = "404", description = "Product type not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "200", description = "Product type edited"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "400", description = "The data provided are invalid, or, the extension of the photo is not supported, or the photo size exceeds the allowed limits.", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be a admin", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PutMapping(value = "/{productId}",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') ")
    public ResponseEntity<Void> modifyProductType(@PathVariable UUID productId, @RequestBody @Valid @ModelAttribute ProductTypeRegistrationDto productTypeRegistrationDto)throws  UnsupportedMediaTypeException, ResourceNotFoundException, LimitExceededException {
        log.info("Modifying product type with id: {}", productTypeRegistrationDto);
        if(!productTypeService.isProductTypePresent(productId)) {
            throw new ResourceNotFoundException(productId.toString(), "product type");
        }
        if(productTypeRegistrationDto.getFeatures().size()>featureProductService.getMaxFeaturesForProduct()) {
            throw new LimitExceededException("A product can have at most " + featureProductService.getMaxFeaturesForProduct() + " features");
        }

        productTypeService.modifyProductType(productId, productTypeRegistrationDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "delete a existing product type", description = "A product type is a template for multiple products. This endpoint allows an admin to delete a product type.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = "Product type not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
        @ApiResponse(responseCode = "409", description = "It' not possible to delete a product type connected to a product", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "204", description = "Product type deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be a admin", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @DeleteMapping(value = "/{productTypeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') ")
    public ResponseEntity<Void> deleteProductType(@PathVariable UUID productTypeId) throws ResourceNotFoundException, ResourceInUseException {
        if(!productTypeService.isProductTypePresent(productTypeId)) {
            throw new ResourceNotFoundException(productTypeId.toString(), "product type");
        }

        if(productTypeService.isProductTypeUsed(productTypeId)) {
            throw new ResourceInUseException("the product type is used in some products");
        }
        productTypeService.deleteProductType(productTypeId);
        return ResponseEntity.noContent().build();
    }


    // metodo per ottenere le carte più vendute
    @Operation(summary = "Get the top seller product types", description = "This endpoint retrieves the top seller product types for a given game. The response will include details such as the product types' names, types, prices, minimum prices, number of sales, associated game, and any relevant features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top seller product types found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping(value = "/top-seller/{game}", produces = "application/json")
    public ResponseEntity<List<ProductTypeDto>> getTopSeller(@PathVariable String game) {
        List<String> games = productTypeService.findDistinctGame();
        for(String g : games){
            if(slugifyService.slugify(g).equals(game)){
                game = g;
                break;
            }
        }
        return ResponseEntity.ok(productTypeService.getTopSeller(game));
    }

    // metodo per ottenere i migliori affari
    @Operation(summary = "Get the best purchases", description = "This endpoint retrieves the best purchases for a given game. The response will include details such as the product types' names, types, prices, minimum prices, number of sales, associated game, and any relevant features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Best purchases found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping(value = "/best-purchases/{game}", produces = "application/json")
    public ResponseEntity<List<ProductTypeDto>> getBestPurchases(@PathVariable String game) {
        List<String> games = productTypeService.findDistinctGame();
        for(String g : games){
            if(slugifyService.slugify(g).equals(game)){
                game = g;
                break;
            }
        }
        return ResponseEntity.ok(productTypeService.getBestPurchases(game));
    }

    // metodo per ottenere un productType
    @Operation(summary = "Get a product type by id", description = "This endpoint retrieves a product type identified by its unique ID. The response will include details such as the product type's name, type, price, minimum price, number of sales, associated game, and any relevant features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product type found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product type not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping(value = "/single/{id}", produces = "application/json")
    public ResponseEntity<ProductTypeDto> getProductType(@PathVariable UUID id) throws ResourceNotFoundException {
         log.info("Get product type with id {}", id);

         ProductTypeDto productTypeDto = productTypeService.getCardType(id);

         if (productTypeDto == null) {
             log.warn("Product type with id {} not found", id);
             throw new ResourceNotFoundException(id.toString(), "product type");
         }

         return ResponseEntity.ok(productTypeDto);
    }

    // metodo per ottenere le cardType di un set
    @Operation(summary = "Get a set of product types", description = "This endpoint retrieves a set of product types identified by their set name. The response will include details such as the product types' names, types, prices, minimum prices, number of sales, associated game, and any relevant features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product types found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping(value = "/set/{setName}", produces = "application/json")
    public ResponseEntity<List<ProductTypeDto>> getCardTypeSet(
            @PathVariable String setName,
            @RequestParam @NotNull String game,
            @RequestParam @NotNull @PositiveOrZero int page,
            @RequestParam @NotNull @Positive @Max(30) int size) throws ResourceNotFoundException {
         log.info("Get product types with set name {} for game {}", setName, game);

         List<ProductTypeDto> productTypeDtos = productTypeService.getCardTypeSet(setName, game, page, size);

         if (productTypeDtos == null || productTypeDtos.isEmpty()) {
             log.warn("Product types with set name {} not found", setName);
             throw new ResourceNotFoundException(setName, "product types");
         }

         return ResponseEntity.ok(productTypeDtos);
    }

    // metodo per ottenere il numero di cardType di un set
    @Operation(summary = "Get the number of product types in a set", description = "This endpoint retrieves the number of product types in a set identified by its set name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Number of product types found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping(value = "/set/number/{setName}", produces = "application/json")
    public ResponseEntity<Long> getCardTypeSetCount(
            @PathVariable String setName,
            @RequestParam @NotNull String game) {
         log.info("Get the number of product types in set {} for game {}", setName, game);

        return ResponseEntity.ok(productTypeService.getCardTypeSetCount(setName, game));
    }

    // metodo per ottenere tutti i productType
    @Operation(summary = "Get all product types", description = "This endpoint retrieves all product types. The response will include details such as the product types' names, types, prices, minimum prices, number of sales, associated game, and any relevant features.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product types found"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping(value = "/all/{type}", produces = "application/json")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('SELLER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<List<ProductTypeDto>> getProductTypes(@PathVariable String type, @RequestParam @NotNull UUID userId, @RequestParam @NotNull int page) {
         log.info("Get all product types for type {}", type);

         return ResponseEntity.ok(productTypeService.getProductTypesSeller(type, page));
    }

    @Operation(summary = "Advanced search", description = "This endpoint performs an advanced search using an external game-related API. It allows users with the 'SELLER' role to conduct a detailed search based on advanced criteria and returning relevant results.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Advanced search"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @GetMapping(value = "/advanced-search/{userId}", produces = "application/json")
    @PreAuthorize("hasRole('SELLER') and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<List<AdvancedSearchDto>> advancedSearch(@PathVariable @NotNull UUID userId, @RequestParam @NotNull @Pattern(regexp = "Pokémon|Magic|Yu-Gi-Oh!", message = "GameType must be 'Pokémon', 'Magic' or 'Yu-gi-Oh!'") String gameType, @RequestParam @NotNull String name) {
         log.info("Advanced search for game type {} and name {}", gameType, name);

         return ResponseEntity.ok(productTypeService.advancedSearch(gameType, name));
    }

    @Operation(summary = "Save a product type", description = "This endpoint allows an authorized user to save a product type from the API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product type saved"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @SecurityRequirements()
    @PostMapping(value = "/save/{gameType}/{userId}", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('SELLER') and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<ProductTypeDto> save(@PathVariable @NotNull UUID userId, @PathVariable @NotNull @Pattern(regexp = "Pokémon|Magic|Yu-Gi-Oh!", message = "GameType must be 'Pokémon', 'Magic' or 'Yu-gi-Oh!'") String gameType, @RequestBody @NotNull String cardId) {
         log.info("Save product type with card id {} and game type {}", cardId, gameType);

         return ResponseEntity.ok(productTypeService.saveAdvancedSearch(cardId, gameType));
    }
}
