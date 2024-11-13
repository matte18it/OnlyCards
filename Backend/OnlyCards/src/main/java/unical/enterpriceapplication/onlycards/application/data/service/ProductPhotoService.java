package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.UUID;

import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductPhoto;

public interface ProductPhotoService {
    void save(ProductPhoto productPhoto2);
    void deleteProductImage(UUID productId, UUID imageId);
    boolean isProductImageExists(UUID productId, UUID imageId);
    int getImagesNumberById(UUID productId);
}
