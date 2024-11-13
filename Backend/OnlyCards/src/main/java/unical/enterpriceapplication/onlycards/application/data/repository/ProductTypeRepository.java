package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, UUID> {
    // Metodo per ottenere tutte le carte
    Page<ProductType> findAll(Specification<ProductType> cardTypeSpecification, Pageable pageable);

    Page<ProductType> findAllByGame(String game, Pageable pageable);

    //Metodo per ottenere tutti i giochi associati ai prodotti
    @Query("SELECT DISTINCT p.game FROM ProductType p")
    List<String> findDistinctGames();

    // Metodo per ottenere una carta in base all'id
    Optional<ProductType> findById(UUID id);

    // Metodo per ottenere le carte più vendute
    @Query(value = "SELECT ct.*, GROUP_CONCAT(CONCAT(f.name, ': ', fc.value) ORDER BY f.name SEPARATOR ', ') AS features FROM product_type ct, feature f, feature_product fc WHERE ct.game = :game AND fc.product_type_id = ct.id AND f.id = fc.feature_id AND ct.min_price_amount > 0 GROUP BY ct.id, ct.name, ct.num_sell ORDER BY ct.num_sell DESC LIMIT 10", nativeQuery = true)
    List<ProductType> findTopSeller(String game);

    // Metodo per ottenere le carte al miglior prezzo
    @Query(value = "SELECT ct.id, ct.name, ct.language, ct.photo, ct.num_sell, ct.last_add, ct.min_price_amount, ct.game, ct.type, c.price_amount, GROUP_CONCAT(CONCAT(f.name, ': ', fc.value) SEPARATOR ', ') AS features FROM product_type ct JOIN product c ON ct.id = c.product_type_id JOIN feature_product fc ON fc.product_type_id = ct.id JOIN feature f ON f.id = fc.feature_id WHERE ct.game = :game AND (ct.min_price_amount > 0 AND c.price_amount > 0) AND c.price_amount = (SELECT MAX(c2.price_amount) FROM product c2 WHERE c2.product_type_id = ct.id) GROUP BY c.price_amount, c.price_currency, ct.id ORDER BY (c.price_amount - ct.min_price_amount) DESC", nativeQuery = true)
    List<Object[]> findBestPurchases(String game, Pageable pageable);
    @Query("SELECT DISTINCT p.language FROM ProductType p")
    List<String> findDistinctLanguages();
    @Query("SELECT DISTINCT p.type FROM ProductType p")
    List<String> findDistinctTypes();

    @Query("SELECT pt FROM ProductType pt " +
            "JOIN FEATURE_PRODUCT fp ON pt.id = fp.productType.id " +
            "JOIN Feature f ON f.id = fp.feature.id " +
            "LEFT JOIN FEATURE_PRODUCT fp2 ON pt.id = fp2.productType.id " +
            "LEFT JOIN Feature f2 ON f2.id = fp2.feature.id AND f2.name = 'set card number' " +
            "WHERE f.name = 'set' AND pt.game = :game AND fp.value = :setName " +
            "ORDER BY CASE WHEN f2.name = 'set card number' THEN fp2.value ELSE fp.value END ASC")
    List<ProductType> findCardTypeSet(String game, String setName, Pageable pageable);
}