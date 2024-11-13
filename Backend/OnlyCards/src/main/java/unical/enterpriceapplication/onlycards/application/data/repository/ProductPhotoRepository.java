package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductPhoto;

import java.util.UUID;

public interface ProductPhotoRepository extends JpaRepository<ProductPhoto, UUID>{
    boolean existsByIdAndProductId(UUID id, UUID productId);

    int countByProductId(UUID productId);
}
