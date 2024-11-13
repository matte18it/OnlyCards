package unical.enterpriceapplication.onlycards.application.data.service;

import org.springframework.data.domain.Page;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductType;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductTypeSpecification;
import unical.enterpriceapplication.onlycards.application.dto.AdvancedSearchDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductTypeDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductTypeRegistrationDto;
import unical.enterpriceapplication.onlycards.application.dto.FeatureSearchDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductTypeService {
    List<FeatureSearchDTO> getFeatures(String game);
    Page<ProductTypeDto> getProductTypes(ProductTypeSpecification.Filter filter, int page, int size);
    ProductType save(ProductType card);
    Optional<ProductType> findById(UUID id);
    boolean isProductTypePresent(UUID cardTypeId);
    ProductTypeDto saveProductType( ProductTypeRegistrationDto productTypeRegistrationDto);
    ProductTypeDto modifyProductType(UUID productId,  ProductTypeRegistrationDto productTypeRegistrationDto);
    List<String> findDistinctGame();
    List<ProductTypeDto> getTopSeller(String type);
    List<ProductTypeDto> getBestPurchases(String type);
    List<String> findDistinctLanguage();
    List<String> findDistinctType();
    boolean isProductTypeUsed(UUID productTypeId);
    void deleteProductType(UUID productTypeId);
    ProductTypeDto getCardType(UUID id);
    List<ProductTypeDto> getCardTypeSet(String setName, String game, int page, int size);
    Long getCardTypeSetCount(String setName, String game);
    List<ProductTypeDto> getProductTypesSeller(String type, int page);
    List<AdvancedSearchDto> advancedSearch(String gameType, String name);
    ProductTypeDto saveAdvancedSearch(String cardId, String gameType);
}
