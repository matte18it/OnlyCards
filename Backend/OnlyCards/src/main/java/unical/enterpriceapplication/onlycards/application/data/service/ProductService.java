package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;
import unical.enterpriceapplication.onlycards.application.dto.ProductCartDTO;
import unical.enterpriceapplication.onlycards.application.dto.ProductDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductEditDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.SaveProductDto;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

public interface ProductService {
    ProductCartDTO getSaleCard(UUID productID) throws ResourceNotFoundException;
    Optional<ProductDto> getProductById(UUID id);
    void deleteProduct(UUID id);
    int getMaxImageForProduct();
    Product save(Product product);
    Optional<Product> findById(UUID id);
    Page<ProductDto> getCardInfo(UUID id, int page);
    Map<String, String> getCurrencies();
    void updateProduct(UUID productId,  ProductEditDto productEditDto);
    boolean isSold(UUID productId);
    void saveProduct(String userId, SaveProductDto saveProductDto);
    List<ProductDto> getLastAddedProducts(String game);
    List<ProductDto> getProductsByUser(UUID userId, int page);
    int countProductById(UUID id);
    Page<ProductInfoDto> getUserProducts(UUID id, int page, int size);
    boolean isUserOwner(UUID id, UUID userId);
}
