package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product,UUID> {

    Page<Product> findAll(Specification<Product> cardSpecification, Pageable pageable);

    @Query("SELECT c FROM Product c JOIN c.wishlists w WHERE  w.id = :wishlistId")
    Page<Product> findAllByWishlistId(UUID wishlistId, Pageable pageable);

    @Query("SELECT c FROM Product c JOIN c.wishlists w WHERE  w.id = :wishlistId AND upper(concat(concat('%',c.user.username ), '%')) like upper(concat(concat('%',:cardOwner ), '%'))")
    Page<Product> findAllByWishlistIdAndProductOwner(UUID wishlistId, Pageable pageable, String cardOwner);

    @Query("SELECT c FROM Product c JOIN c.wishlists w WHERE  w.id = :wishlistId AND upper(concat(concat('%',c.productType.name ), '%')) like upper(concat(concat('%',:cardName ), '%'))")
    Page<Product> findAllByWishlistIdAndProductName(UUID wishlistId, Pageable pageable, String cardName);

    @Query("SELECT c FROM Product c JOIN c.wishlists w WHERE  w.id = :wishlistId AND upper(concat(concat('%',c.user.username ), '%')) like upper(concat(concat('%',:cardOwner ), '%')) AND upper(concat(concat('%',c.productType.name ), '%')) like upper(concat(concat('%',:cardName ), '%'))")
    Page<Product> findAllByWishlistIdAndProductNameAndProductOwner(UUID wishlistId, Pageable pageable, String cardName, String cardOwner);

    @Query("SELECT DISTINCT p FROM Product p JOIN FETCH p.productType pt JOIN FETCH p.user u WHERE pt.id = :id AND p.sold = false")
    Page<Product> findAllByProductId(UUID id, Pageable pageable);

    @Query("SELECT p, pt FROM Product p, ProductType pt WHERE p.productType.id = pt.id AND pt.game = :game AND p.sold = false ORDER BY p.releaseDate DESC")
    Page<Object> findAllLastAdd(String game, Pageable pageable);

    @Query("SELECT p, pt FROM Product p, ProductType pt WHERE p.productType.id = pt.id AND p.user.id = :userId ORDER BY p.releaseDate DESC, p.sold ASC")
    Page<Object> findAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.user.id = :userId AND p.sold = false ")
    Page<Product> findAllByUserIdAndSold(UUID userId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p, User u WHERE p.user.id = u.id AND u.id = :id")
    int countProductById(UUID id);

    boolean existsByIdAndSoldTrue(UUID id);
}
