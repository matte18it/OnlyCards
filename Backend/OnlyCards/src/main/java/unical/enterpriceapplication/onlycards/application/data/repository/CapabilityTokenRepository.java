package unical.enterpriceapplication.onlycards.application.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.CapabilityToken;

import java.util.Optional;
import java.util.UUID;

public  interface CapabilityTokenRepository extends JpaRepository<CapabilityToken, UUID> {
    Optional<CapabilityToken> findByIdAndWishlist_Id(UUID id, UUID wishlistId);

    Optional<CapabilityToken> findByWishlist_Id(UUID wishlistId);

    Optional<CapabilityToken> findByToken(String token);
}
