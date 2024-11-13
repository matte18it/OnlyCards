package unical.enterpriceapplication.onlycards.application.data.repository;


import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.Wishlist;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.WishlistOwnership;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID>{

    @Query("SELECT COUNT(w) FROM Wishlist w JOIN w.users aw WHERE aw.user.id = :userId and aw.ownership = 'OWNER'")
    Integer countWishlistByOwner(UUID userId);

    @Query("SELECT w FROM Wishlist w JOIN w.users aw WHERE aw.user.id = :userId and aw.ownership = :ownership")
    Page<Wishlist> findByUserIdAndOwnership(UUID userId, WishlistOwnership ownership, Pageable pageable);



    @Query("SELECT w FROM Wishlist w JOIN w.users aw WHERE aw.user.id = :userId")
    Page<Wishlist> findByUserId(UUID userId, Pageable pageable);


    @Query("SELECT w FROM Wishlist w JOIN w.users aw WHERE aw.user.id = :userId AND w.isPublic = true AND aw.ownership = OWNER")
    Page<Wishlist> findPublicWishlistsByUserId(UUID userId, Pageable pageable);


    @Query("SELECT w FROM Wishlist w JOIN w.users aw WHERE aw.user.id = :userId and w.name = :wishlistName and aw.ownership='OWNER' and w.isPublic=true")
    Optional<Wishlist> findByNameAndUserOwnerAndPublic(String wishlistName, UUID userId);

    @Query("SELECT w FROM Wishlist w JOIN w.users aw WHERE aw.user.id = :userId and w.name = :wishlistName and aw.ownership='OWNER' ")
    Optional<Wishlist> findByNameAndUserOwner(String wishlistName, UUID userId);


}
