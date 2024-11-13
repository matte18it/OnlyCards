package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Feature;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.FeatureProduct;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductType;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FeatureProductRepository extends JpaRepository<FeatureProduct, Long>{
    @Query("SELECT distinct fc FROM FEATURE_PRODUCT fc WHERE fc.productType.game = :game")
    Set<FeatureProduct> findByCardTypeGame(String game);

    Optional<FeatureProduct> findByFeatureAndProductType(Feature feature, ProductType productType);

    List<FeatureProduct> findByProductType_Id(UUID cardTypeId);
    void deleteByProductType(ProductType productType);
}
