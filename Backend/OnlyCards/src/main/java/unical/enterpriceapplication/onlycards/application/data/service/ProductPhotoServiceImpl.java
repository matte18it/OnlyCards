package unical.enterpriceapplication.onlycards.application.data.service;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import unical.enterpriceapplication.onlycards.application.core.service.FirebaseFolders;
import unical.enterpriceapplication.onlycards.application.core.service.FirebaseStorageService;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductPhoto;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductPhotoRepository;

@Service
@RequiredArgsConstructor
public class ProductPhotoServiceImpl implements ProductPhotoService {
    private final ProductPhotoRepository productPhotoRepository;
    private final FirebaseStorageService firebaseStorageService;

    @Override
    public void save(ProductPhoto productPhoto) {
        productPhotoRepository.saveAndFlush(productPhoto);
    }

    @Override
    @Transactional
    public void deleteProductImage(UUID productId, UUID imageId) {
       Optional<ProductPhoto> photo = productPhotoRepository.findById(imageId);
         if(photo.isPresent() && photo.get().getProduct().getId().equals(productId)){
              productPhotoRepository.delete(photo.get());
                firebaseStorageService.deleteFile(String.valueOf(photo.get().getId()), FirebaseFolders.PRODUCT);
         }
    }

    @Override
    public boolean isProductImageExists(UUID productId, UUID imageId) {
        return productPhotoRepository.existsByIdAndProductId(imageId, productId);
    }

    @Override
    public int getImagesNumberById(UUID productId) {
        return productPhotoRepository.countByProductId(productId);
    }
}
