package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.UserWishlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountWishlistRepository  extends JpaRepository<UserWishlist, Long>{
     void deleteAllByWishlist_Id(UUID id);

    List<UserWishlist> findByWishlist_Id(UUID id);
    List<UserWishlist> findByUser_Id(UUID id);
    Optional<UserWishlist> findByUser_IdAndWishlist_Id(UUID accountId, UUID wishlistId);

}
